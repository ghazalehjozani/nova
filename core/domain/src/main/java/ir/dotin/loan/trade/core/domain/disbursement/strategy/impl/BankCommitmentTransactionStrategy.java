package ir.dotin.loan.trade.core.domain.disbursement.strategy.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionCause;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionType;
import ir.dotin.loan.baseloan.core.domain.shared.i18n.ValidationLocalizedMessageCodes;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByRelationTypeClient;
import ir.dotin.loan.baseloan.core.domain.shared.service.transaction.ArticleCommentFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.AbstractDocumentItemStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.Article;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.NetworkInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TerminalInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TransactionInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TransactionMetadata;
import ir.dotin.loan.trade.core.domain.disbursement.strategy.CommitmentHandlingStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.aggregate.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

@DomainService
public final class BankCommitmentTransactionStrategy extends AbstractDocumentItemStrategy<TradeLoanFacility>
        implements CommitmentHandlingStrategy {

    private static final TransactionType TRANSACTION_TYPE = TransactionType.CODE_11009;
    private static final TransactionCause TRANSACTION_CAUSE = TransactionCause.SET_BANK_COMMITMENT;

    public BankCommitmentTransactionStrategy(
            FindAccountByRelationTypeClient findAccountClient, ArticleCommentFactory commentFactory) {
        super(findAccountClient, commentFactory);
    }

    @Override
    public Result<List<Article>> calculateItems(CalculationContext<TradeLoanFacility> context) {
        return calculateItemsInternal(context, "Incomplete articles for Bank Commitment");
    }

    @Override
    protected Result<List<Article>> generateItems(CalculationContext<TradeLoanFacility> context) {
        Objects.requireNonNull(context, "CalculationContext cannot be null.");

        Money amount = context.principalAmount();
        LoanTopic topic = context.primaryLoanTopic();
        Optional<TransactionMetadata> baseMetadataOpt = context.baseMetadata();
        PostTitle postTitle = context.postTitle();

        Notification accumulatedNotification = Notification.create();
        List<Article> items = new ArrayList<>();

        Result<TransactionInfo> trxInfoResult = TransactionInfo.of(TRANSACTION_TYPE, TRANSACTION_CAUSE);
        if (trxInfoResult.isFailure()) {
            return Result.failure(accumulatedNotification.merge(trxInfoResult.notification()));
        }

        TransactionInfo validTrxInfo = trxInfoResult.value();

        Result<TransactionMetadata> resolvedMetadataResult = baseMetadataOpt
                .map(bm -> {
                    TransactionMetadata updatedBm = bm.withTransactionInfo(validTrxInfo);
                    Notification validation = updatedBm.validate();
                    return validation.hasErrors()
                            ? Result.<TransactionMetadata>failure(validation)
                            : Result.success(updatedBm);
                })
                .orElseGet(() -> {
                    Result<TerminalInfo> defaultTerminalInfoResult = TerminalInfo.of("DEFAULT_TERMINAL_ID_PLACEHOLDER");
                    Result<NetworkInfo> defaultNetworkInfoResult = NetworkInfo.of("DEFAULT_NETWORK_CODE_PLACEHOLDER");

                    return Result.combine(
                                    defaultTerminalInfoResult,
                                    defaultNetworkInfoResult,
                                    (termInfo, netInfo) ->
                                            TransactionMetadata.of(validTrxInfo, termInfo, netInfo, null, null, null))
                            .flatMap(Function.identity());
                });

        accumulatedNotification = accumulatedNotification.merge(resolvedMetadataResult.notification());
        if (resolvedMetadataResult.isFailure()) {
            if (!accumulatedNotification.hasErrors()) {
                accumulatedNotification =
                        accumulatedNotification.addError(ValidationLocalizedMessageCodes.DOCUMENT_CREATION_FAILED);
            }
            return Result.failure(accumulatedNotification);
        }
        TransactionMetadata resolvedMetadata = resolvedMetadataResult.value();

        TradeRelationType debitRelation = TradeRelationType.BANK_COMMITMENTS_CONTRA;
        Result<Article> debitItemResult =
                findAndCreateAccountItem(debitRelation, amount, topic, postTitle, resolvedMetadata);
        accumulatedNotification = accumulatedNotification.merge(debitItemResult.notification());
        debitItemResult.ifSuccess(items::add);

        TradeRelationType creditRelation = TradeRelationType.BANK_COMMITMENTS;
        Result<Article> creditItemResult =
                findAndCreateAccountItem(creditRelation, amount, topic, postTitle, resolvedMetadata);
        accumulatedNotification = accumulatedNotification.merge(creditItemResult.notification());
        creditItemResult.ifSuccess(items::add);

        if (accumulatedNotification.hasErrors()) {
            return Result.failure(accumulatedNotification);
        }

        if (items.isEmpty() && amount != null && amount.isPositive()) {
            accumulatedNotification =
                    accumulatedNotification.addError(ValidationLocalizedMessageCodes.STRATEGY_RETURNED_NO_ITEMS);
            return Result.failure(accumulatedNotification);
        }
        return Result.success(items);
    }
}
