package ir.dotin.loan.trade.core.domain.shared.document.transaction;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.accounting.document.api.model.ArticleComponent;
import ir.dotin.platform.accounting.document.api.model.ArticleType;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.PostTitle;
import ir.dotin.platform.accounting.document.api.model.metadata.ArticleMetadata;
import ir.dotin.platform.accounting.document.api.strategy.DocumentCalculationStrategy;
import ir.dotin.platform.accounting.document.core.assembly.DocumentAssembler;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainService;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.error.TradeLoanFacilityErrors;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.builder.TradeArticleComponentBuilder;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DisburseBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.PaymentAmountArticleType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.DisbursementStrategyProvider;

import static java.util.Objects.requireNonNull;

@DomainService
public class IrregularProgressiveDisbursementTransactionService {

    private final DocumentAssembler documentAssembler;
    private final DisbursementStrategyProvider strategyProvider;
    private final Clock clock;

    public IrregularProgressiveDisbursementTransactionService(
            DocumentAssembler documentAssembler, DisbursementStrategyProvider strategyProvider, Clock clock) {
        this.documentAssembler = requireNonNull(documentAssembler);
        this.strategyProvider = requireNonNull(strategyProvider);
        this.clock = requireNonNull(clock);
    }

    public Result<List<LoanTransaction>> createTransactions(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            @NonNull InstallmentSchedule currentSchedule,
            @NonNull List<Installment> recalculatedInstallments,
            @NonNull Money trancheAmount,
            ResolvedAccounts resolvedAccounts) {

        return calculateIncrementalInterest(facility.isFirstDisbursement(), currentSchedule, recalculatedInstallments)
                .flatMap(incrementalInterest -> {
                    List<Result<LoanTransaction>> results = List.of(
                            createBankCommitmentTransaction(
                                    facility,
                                    loanType,
                                    branchCode,
                                    trancheAmount,
                                    postTitle,
                                    baseMetadata,
                                    resolvedAccounts),
                            createPaymentAmountTransaction(
                                    facility,
                                    loanType,
                                    branchCode,
                                    trancheAmount,
                                    postTitle,
                                    baseMetadata,
                                    resolvedAccounts),
                            createDisbursedInterestTransaction(
                                    facility,
                                    loanType,
                                    branchCode,
                                    incrementalInterest,
                                    postTitle,
                                    baseMetadata,
                                    resolvedAccounts));
                    return Result.traverse(results, result -> result);
                });
    }

    public Result<LoanTransaction> createBankCommitmentTransaction(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull Money amount,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            ResolvedAccounts resolvedAccounts) {

        return newComponentBuilder(facility, loanType)
                .buildSinglePair(
                        DisburseBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG,
                        DisburseBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG,
                        amount,
                        resolvedAccounts)
                .flatMap(components -> facility.getSanctionedLoan()
                        .map(sl -> assembleAndWrap(
                                facility,
                                sl.getCurrency(),
                                branchCode,
                                components,
                                postTitle,
                                findStrategy(DisburseBankCommitmentArticleType.class, facility),
                                baseMetadata,
                                resolvedAccounts))
                        .orElseGet(() -> Result.failure(
                                Notification.ofError(TradeLoanFacilityErrors.SANCTIONED_LOAN_NOT_FOUND_FOR_FACILITY))));
    }

    public Result<LoanTransaction> createPaymentAmountTransaction(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull Money amount,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            ResolvedAccounts resolvedAccounts) {

        return newComponentBuilder(facility, loanType)
                .buildSinglePairWithContext(
                        PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG,
                        PaymentAmountArticleType.DISBURSEMENT_CREDIT,
                        amount,
                        facility.getLoanApplication().getDisburseDestination(),
                        resolvedAccounts)
                .flatMap(components -> facility.getSanctionedLoan()
                        .map(sl -> assembleAndWrap(
                                facility,
                                sl.getCurrency(),
                                branchCode,
                                components,
                                postTitle,
                                findStrategy(PaymentAmountArticleType.class, facility),
                                baseMetadata,
                                resolvedAccounts))
                        .orElseGet(() -> Result.failure(
                                Notification.ofError(TradeLoanFacilityErrors.SANCTIONED_LOAN_NOT_FOUND))));
    }

    public Result<LoanTransaction> createDisbursedInterestTransaction(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull Money interestAmount,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            ResolvedAccounts resolvedAccounts) {

        return newComponentBuilder(facility, loanType)
                .buildSinglePair(
                        DisbursedInterestArticleType.INTEREST_DEBIT_LEG,
                        DisbursedInterestArticleType.INTEREST_CREDIT_LEG,
                        interestAmount,
                        resolvedAccounts)
                .flatMap(components -> facility.getSanctionedLoan()
                        .map(sl -> assembleAndWrap(
                                facility,
                                sl.getCurrency(),
                                branchCode,
                                components,
                                postTitle,
                                findStrategy(DisbursedInterestArticleType.class, facility),
                                baseMetadata,
                                resolvedAccounts))
                        .orElseGet(() -> Result.failure(
                                Notification.ofError(TradeLoanFacilityErrors.SANCTIONED_LOAN_NOT_FOUND))));
    }

    // ── Internal helpers ─────────────────────────────────────────

    private TradeArticleComponentBuilder newComponentBuilder(TradeLoanFacility facility, TradeLoanType loanType) {
        return new TradeArticleComponentBuilder(
                loanType.getRelationTypeLoanTopics(),
                requireNonNull(facility.getLoanApplication()).getEconomicSector());
    }

    /** Assembles an accounting document and wraps it into a loan-domain {@link LoanTransaction}. */
    private <K extends Enum<K> & ArticleType<K, TradeRelationType>> Result<LoanTransaction> assembleAndWrap(
            TradeLoanFacility facility,
            CurrencyType currency,
            BranchCode branchCode,
            Map<K, ArticleComponent> components,
            PostTitle postTitle,
            DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, K> strategy,
            ArticleMetadata baseMetadata,
            ResolvedAccounts resolvedAccounts) {

        return documentAssembler
                .assemble(
                        facility,
                        currency,
                        branchCode,
                        components,
                        postTitle,
                        strategy,
                        baseMetadata,
                        resolvedAccounts::getAccount)
                .flatMap(document -> LoanTransaction.of(facility.getId(), document, clock));
    }

    @SuppressWarnings("unchecked")
    private <K extends Enum<K> & ArticleType<K, TradeRelationType>>
            DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, K> findStrategy(
                    Class<K> articleTypeClass, TradeLoanFacility facility) {

        return strategyProvider.getStrategies(facility).stream()
                .filter(s -> s.getArticleTypeClass().equals(articleTypeClass))
                .findFirst()
                .map(s -> (DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, K>) s)
                .orElseThrow(() -> new IllegalStateException("No strategy found for: " + articleTypeClass));
    }

    private Result<Money> calculateIncrementalInterest(
            boolean firstDisbursement,
            @NonNull InstallmentSchedule currentSchedule,
            @NonNull List<Installment> recalculatedInstallments) {

        Money newTotalInterest = sumInterestAmount(recalculatedInstallments, currentSchedule.getCurrency());

        if (firstDisbursement) {
            return Result.success(newTotalInterest);
        }

        Money currentTotalInterest =
                sumInterestAmount(currentSchedule.getInstallments(), currentSchedule.getCurrency());
        Money incrementalInterest = newTotalInterest
                .subtract(currentTotalInterest)
                .unwrapOrThrow(c -> new IllegalStateException("Interest calculation failed"));

        if (incrementalInterest.isNegative()) {
            return Result.failure(
                    TradeLoanFacilityErrors.INCREMENTAL_INTEREST_CANNOT_BE_NEGATIVE,
                    newTotalInterest,
                    currentTotalInterest);
        }
        return Result.success(incrementalInterest);
    }

    private Money sumInterestAmount(List<Installment> installments, CurrencyType currency) {
        return installments.stream()
                .map(installment -> installment.getScheduledAmount().interestAmount())
                .reduce(
                        Money.zero(currency).unwrap(),
                        (sum, next) -> sum.add(next).unwrap());
    }
}
