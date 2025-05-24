package ir.dotin.loan.trade.core.domain.disbursement.strategy.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DisburseDestination;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionCause;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.Direction;
import ir.dotin.loan.baseloan.core.domain.shared.i18n.ValidationLocalizedMessageCodes;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByRelationTypeClient;
import ir.dotin.loan.baseloan.core.domain.shared.service.transaction.ArticleCommentFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.AbstractDocumentItemStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.Article;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.ArticleComment;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.DepositNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.NetworkInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TerminalInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TransactionInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TransactionMetadata;
import ir.dotin.loan.trade.core.domain.disbursement.i18n.TradeDisbursementLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.disbursement.strategy.CashMovementStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.aggregate.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

@DomainService
public final class PaymentAmountTransactionStrategy extends AbstractDocumentItemStrategy<TradeLoanFacility>
        implements CashMovementStrategy {

    public PaymentAmountTransactionStrategy(
            FindAccountByRelationTypeClient findAccountClient, ArticleCommentFactory commentFactory) {
        super(findAccountClient, commentFactory);
    }

    @Override
    public Result<List<Article>> calculateItems(CalculationContext<TradeLoanFacility> context) {
        return calculateItemsInternal(context, "Incomplete articles for Payment Amount");
    }

    @Override
    protected Result<List<Article>> generateItems(CalculationContext<TradeLoanFacility> context) {
        Objects.requireNonNull(context, "CalculationContext cannot be null.");

        Money amount = context.principalAmount();
        LoanTopic topic = context.primaryLoanTopic();
        PostTitle postTitle = context.postTitle();
        Optional<TransactionMetadata> baseMetadataOpt = context.baseMetadata();

        Notification accumulatedNotification = Notification.create();
        List<Article> items = new ArrayList<>();

        TransactionMetadata resolvedMetadata;
        if (baseMetadataOpt.isPresent()) {
            resolvedMetadata = baseMetadataOpt.orElseThrow();
        } else {
            Result<TransactionInfo> defaultTrxInfoResult =
                    TransactionInfo.of(TransactionType.CODE_10004, TransactionCause.LRPA);
            Result<TerminalInfo> defaultTerminalInfoResult = TerminalInfo.of("DEFAULT_TERMINAL");
            Result<NetworkInfo> defaultNetworkInfoResult = NetworkInfo.of("INTERNAL");

            if (defaultTrxInfoResult.isFailure()
                    || defaultTerminalInfoResult.isFailure()
                    || defaultNetworkInfoResult.isFailure()) {
                accumulatedNotification = accumulatedNotification
                        .merge(defaultTrxInfoResult.notification())
                        .merge(defaultTerminalInfoResult.notification())
                        .merge(defaultNetworkInfoResult.notification())
                        .addError(TradeDisbursementLocalizedMessageCodes.TRANSACTION_METADATA_DEFAULT_CREATION_FAILED);
                return Result.failure(accumulatedNotification);
            }

            Result<TransactionMetadata> defaultMetadataResult = TransactionMetadata.of(
                    defaultTrxInfoResult.value(),
                    defaultTerminalInfoResult.value(),
                    defaultNetworkInfoResult.value(),
                    null,
                    null,
                    null);

            if (defaultMetadataResult.isFailure()) {
                accumulatedNotification = accumulatedNotification
                        .merge(defaultMetadataResult.notification())
                        .addError(TradeDisbursementLocalizedMessageCodes.TRANSACTION_METADATA_DEFAULT_CREATION_FAILED);
                return Result.failure(accumulatedNotification);
            }
            resolvedMetadata = defaultMetadataResult.value();
        }

        TradeRelationType debitRelation = TradeRelationType.PRINCIPAL;
        Result<Article> debitItemResult =
                findAndCreateAccountItem(debitRelation, amount, topic, postTitle, resolvedMetadata);

        if (debitItemResult.isSuccess()) {
            items.add(debitItemResult.value());
        } else {
            accumulatedNotification = accumulatedNotification.merge(debitItemResult.notification());
        }

        if (!accumulatedNotification.hasErrors()) {
            DisburseDestination destination =
                    context.loanFacility().getLoanApplication().getDisburseDestination();
            Objects.requireNonNull(destination, "DisburseDestination cannot be null.");
            Objects.requireNonNull(destination.type(), "DisburseDestinationType cannot be null.");

            Result<Article> creditItemResult = null;
            Result<ArticleComment> creditorCommentResult;

            switch (destination.type()) {
                case BOX -> {
                    creditorCommentResult = commentFactory.createBoxComment(Direction.CREDITOR, postTitle);
                    if (creditorCommentResult.isSuccess()) {
                        creditItemResult = Article.ofBox(
                                amount, Direction.CREDITOR, creditorCommentResult.value(), resolvedMetadata);
                    } else {
                        accumulatedNotification = accumulatedNotification.merge(creditorCommentResult.notification());
                    }
                }
                case DEPOSIT -> {
                    Optional<DepositNumber> depositNumberOpt = destination.depositNumber();
                    if (depositNumberOpt.isEmpty()) {
                        accumulatedNotification = accumulatedNotification.addError(
                                TradeDisbursementLocalizedMessageCodes.DEPOSIT_NUMBER_REQUIRED_FOR_DEPOSIT_TYPE);
                    } else {
                        DepositNumber actualDepositNumber = depositNumberOpt.orElseThrow();
                        creditorCommentResult =
                                commentFactory.createDepositComment(Direction.CREDITOR, actualDepositNumber, postTitle);
                        if (creditorCommentResult.isSuccess()) {
                            creditItemResult = Article.ofDeposit(
                                    actualDepositNumber,
                                    amount,
                                    Direction.CREDITOR,
                                    creditorCommentResult.value(),
                                    resolvedMetadata);
                        } else {
                            accumulatedNotification =
                                    accumulatedNotification.merge(creditorCommentResult.notification());
                        }
                    }
                }
                default ->
                    accumulatedNotification = accumulatedNotification.addError(
                            ValidationLocalizedMessageCodes.ARTICLE_INVALID_TARGET_TYPE_FOR_CREDIT,
                            destination.type().name());
            }

            if (creditItemResult != null) {
                if (creditItemResult.isSuccess()) {
                    items.add(creditItemResult.value());
                } else {
                    accumulatedNotification = accumulatedNotification.merge(creditItemResult.notification());
                }
            }
        }

        if (accumulatedNotification.hasErrors()) {
            return Result.failure(accumulatedNotification);
        }

        return Result.success(items);
    }
}
