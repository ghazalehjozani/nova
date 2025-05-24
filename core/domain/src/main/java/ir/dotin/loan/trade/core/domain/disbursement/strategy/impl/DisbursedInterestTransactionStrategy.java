package ir.dotin.loan.trade.core.domain.disbursement.strategy.impl;

import java.util.*;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InterestPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionCause;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionType;
import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
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
import ir.dotin.loan.trade.core.domain.disbursement.formula.TradeInterestCalculationService;
import ir.dotin.loan.trade.core.domain.disbursement.i18n.TradeDisbursementLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.disbursement.strategy.DisbursedInterestFacilitiesStrategy;
import ir.dotin.loan.trade.core.domain.loanarrangement.aggregate.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanfacility.aggregate.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.i18n.TradeLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.interaction.TradeLoanArrangementDataProvider;

@DomainService
public final class DisbursedInterestTransactionStrategy extends AbstractDocumentItemStrategy<TradeLoanFacility>
        implements DisbursedInterestFacilitiesStrategy {

    private final TradeLoanArrangementDataProvider arrangementDataProvider;
    private final TradeInterestCalculationService interestCalculationService;

    public DisbursedInterestTransactionStrategy(
            FindAccountByRelationTypeClient findAccountClient,
            ArticleCommentFactory commentFactory,
            TradeLoanArrangementDataProvider arrangementDataProvider,
            TradeInterestCalculationService interestCalculationService) {
        super(findAccountClient, commentFactory);
        this.arrangementDataProvider = Objects.requireNonNull(arrangementDataProvider);
        this.interestCalculationService = Objects.requireNonNull(interestCalculationService);
    }

    @Override
    public Result<List<Article>> calculateItems(CalculationContext<TradeLoanFacility> context) {
        return calculateItemsInternal(context, "Incomplete articles for Disbursed Facilities Interest");
    }

    @Override
    protected Result<List<Article>> generateItems(CalculationContext<TradeLoanFacility> context) {
        Objects.requireNonNull(context, "CalculationContext cannot be null.");

        Money principal = context.principalAmount();
        if (principal.isZero()) {
            return Result.success(Collections.emptyList());
        }

        Notification accumulatedNotification = Notification.create();
        List<Article> items = new ArrayList<>();

        TradeLoanArrangementId arrangementId = context.loanFacility().getLoanArrangementId();
        Result<TradeLoanArrangement> arrangementResult = arrangementDataProvider.findArrangementById(arrangementId);
        if (arrangementResult.isFailure()) {
            accumulatedNotification = accumulatedNotification
                    .merge(arrangementResult.notification())
                    .addError(TradeLoanFacilityLocalizedMessageCodes.ARRANGEMENT_FETCH_FAILED, arrangementId.value());
            return Result.failure(accumulatedNotification);
        }
        TradeLoanArrangement arrangement = arrangementResult.value();
        InterestPolicy<BaseFormulaField> interestPolicy = arrangement.getInterestPolicy();

        Result<Money> interestAmountResult =
                interestCalculationService.calculateTotalInterest(interestPolicy, context.loanFacility());

        if (interestAmountResult.isFailure()) {
            accumulatedNotification = accumulatedNotification.merge(interestAmountResult.notification());
            return Result.failure(accumulatedNotification);
        }
        Money totalInterestAmount = interestAmountResult.value();

        LoanTopic topic = context.primaryLoanTopic();
        PostTitle postTitle = context.postTitle();
        Optional<TransactionMetadata> baseMetadataOpt = context.baseMetadata();

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
                findAndCreateAccountItem(debitRelation, totalInterestAmount, topic, postTitle, resolvedMetadata);

        if (debitItemResult.isSuccess()) {
            items.add(debitItemResult.value());
        } else {
            accumulatedNotification = accumulatedNotification.merge(debitItemResult.notification());
        }

        TradeRelationType creditRelation = TradeRelationType.FUTURE_INTEREST;
        Result<Article> creditItemResult =
                findAndCreateAccountItem(creditRelation, totalInterestAmount, topic, postTitle, resolvedMetadata);

        if (creditItemResult.isSuccess()) {
            items.add(creditItemResult.value());
        } else {
            accumulatedNotification = accumulatedNotification.merge(creditItemResult.notification());
        }

        if (accumulatedNotification.hasErrors()) {
            return Result.failure(accumulatedNotification);
        }

        return Result.success(items);
    }
}
