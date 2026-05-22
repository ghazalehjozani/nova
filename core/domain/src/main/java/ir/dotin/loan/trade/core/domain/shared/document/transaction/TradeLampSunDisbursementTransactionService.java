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
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;
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
public class TradeLampSunDisbursementTransactionService {

    private final DocumentAssembler documentAssembler;
    private final DisbursementStrategyProvider strategyProvider;
    private final Clock clock;

    public TradeLampSunDisbursementTransactionService(
            DocumentAssembler documentAssembler, DisbursementStrategyProvider strategyProvider, Clock clock) {
        this.documentAssembler = requireNonNull(documentAssembler);
        this.strategyProvider = requireNonNull(strategyProvider);
        this.clock = requireNonNull(clock);
    }

    public Result<List<LoanTransaction>> createTransactions(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanArrangement loanArrangement,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            @NonNull InstallmentSchedule installmentSchedule,
            ResolvedAccounts resolvedAccounts) {

        return facility.getSanctionedLoan()
                .map(sanctionedLoan -> buildTransactions(
                        facility,
                        loanArrangement,
                        sanctionedLoan,
                        loanType,
                        branchCode,
                        postTitle,
                        baseMetadata,
                        installmentSchedule,
                        resolvedAccounts))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanFacilityErrors.SANCTIONED_LOAN_NOT_FOUND_FOR_FACILITY,
                        facility.getId().value())));
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

    private Result<List<LoanTransaction>> buildTransactions(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanArrangement tradeLoanArrangement,
            @NonNull TradeSanctionedLoan sanctionedLoan,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            @NonNull InstallmentSchedule installmentSchedule,
            ResolvedAccounts resolvedAccounts) {

        Money interestAmount = calculateInterestAmount(tradeLoanArrangement, installmentSchedule);
        Money approvedAmount = sanctionedLoan.getApprovedAmount();

        List<Result<LoanTransaction>> results = List.of(
                createBankCommitmentTransaction(
                        facility,
                        loanType,
                        branchCode,
                        requireNonNull(approvedAmount),
                        postTitle,
                        baseMetadata,
                        resolvedAccounts),
                createPaymentAmountTransaction(
                        facility, loanType, branchCode, approvedAmount, postTitle, baseMetadata, resolvedAccounts),
                createDisbursedInterestTransaction(
                        facility, loanType, branchCode, interestAmount, postTitle, baseMetadata, resolvedAccounts));

        return Result.traverse(results, result -> result);
    }

    private Money calculateInterestAmount(
            @NonNull TradeLoanArrangement arrangement, @NonNull InstallmentSchedule installmentSchedule) {

        InstallmentPaymentType paymentType = arrangement.getInstallmentPolicy().installmentPaymentType();

        if (paymentType == InstallmentPaymentType.GRADUAL) {
            return installmentSchedule.getInstallments().stream()
                    .map(installment -> installment.getScheduledAmount().interestAmount())
                    .reduce((money, other) -> money.add(other).unwrap())
                    .orElseThrow(() ->
                            new IllegalStateException("No installments found in schedule for GRADUAL payment type"));
        } else {
            throw new UnsupportedOperationException("Not Implemented");
        }
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
}
