package ir.dotin.loan.trade.core.domain.loanfacility.service.transaction;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.annotation.DomainService;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.builder.ArticleComponentMapBuilder;
import ir.dotin.loan.baseloan.core.domain.shared.service.DocumentBuilderService;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.ArticleMetadata;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisburseBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.PaymentAmountArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.i18n.TradeLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.DisbursementStrategyProvider;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

@DomainService
public class IrregularProgressiveDisbursementTransactionService {

    private final DocumentBuilderService documentBuilderService;
    private final DisbursementStrategyProvider strategyProvider;

    public IrregularProgressiveDisbursementTransactionService(
            DocumentBuilderService documentBuilderService, DisbursementStrategyProvider strategyProvider) {
        this.documentBuilderService = requireNonNull(documentBuilderService);
        this.strategyProvider = requireNonNull(strategyProvider);
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
        return calculateIncrementalInterest(
                        facility.getSanctionedLoan().orElseThrow().isFirstDisbursement(),
                        currentSchedule,
                        recalculatedInstallments)
                .flatMap(incrementalInterest -> {
                    List<Result<LoanTransaction>> transactionResults = ImmutableList.of(
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

                    return Result.traverse(transactionResults, result -> result);
                });
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
                .orElseThrow(() -> new IllegalStateException("Interest calculation failed"));

        if (incrementalInterest.isNegative()) {
            return Result.failure(Notification.ofError(
                    TradeLoanFacilityLocalizedMessageCodes.INCREMENTAL_INTEREST_CANNOT_BE_NEGATIVE,
                    newTotalInterest,
                    currentTotalInterest));
        }

        return Result.success(incrementalInterest);
    }

    private Money sumInterestAmount(List<Installment> installments, CurrencyType currency) {
        return installments.stream()
                .map(installment -> installment.getScheduledAmount().interestAmount())
                .reduce(Money.zero(currency).orElseThrow(), (money, other) -> money.add(other)
                        .orElseThrow());
    }

    private Result<LoanTransaction> createBankCommitmentTransaction(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull Money amount,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            ResolvedAccounts resolvedAccounts) {

        ArticleComponentMapBuilder<TradeRelationType> builder = new ArticleComponentMapBuilder<>(
                loanType.getRelationTypeLoanTopics(),
                requireNonNull(facility.getLoanApplication()).getEconomicSector());

        return builder.buildSinglePair(
                        DisburseBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG,
                        DisburseBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG,
                        amount,
                        resolvedAccounts)
                .flatMap(components -> facility.getSanctionedLoan()
                        .map(sanctionedLoan -> documentBuilderService.buildTransaction(
                                facility,
                                sanctionedLoan.getCurrency(),
                                branchCode,
                                components,
                                postTitle,
                                findStrategy(DisburseBankCommitmentArticleType.class, facility),
                                baseMetadata,
                                resolvedAccounts))
                        .orElseGet(() -> Result.failure(Notification.ofError(
                                TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND_FOR_FACILITY))));
    }

    private Result<LoanTransaction> createPaymentAmountTransaction(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull Money amount,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            ResolvedAccounts resolvedAccounts) {

        ArticleComponentMapBuilder<TradeRelationType> builder = new ArticleComponentMapBuilder<>(
                loanType.getRelationTypeLoanTopics(),
                requireNonNull(facility.getLoanApplication()).getEconomicSector());

        return builder.buildSinglePairWithContext(
                        PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG,
                        PaymentAmountArticleType.DISBURSEMENT_CREDIT,
                        amount,
                        facility.getLoanApplication().getDisburseDestination(),
                        resolvedAccounts)
                .flatMap(components -> facility.getSanctionedLoan()
                        .map(sanctionedLoan -> documentBuilderService.buildTransaction(
                                facility,
                                sanctionedLoan.getCurrency(),
                                branchCode,
                                components,
                                postTitle,
                                findStrategy(PaymentAmountArticleType.class, facility),
                                baseMetadata,
                                resolvedAccounts))
                        .orElseGet(() -> Result.failure(Notification.ofError(
                                TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND))));
    }

    private Result<LoanTransaction> createDisbursedInterestTransaction(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull Money interestAmount,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            ResolvedAccounts resolvedAccounts) {

        ArticleComponentMapBuilder<TradeRelationType> builder = new ArticleComponentMapBuilder<>(
                loanType.getRelationTypeLoanTopics(),
                facility.getLoanApplication().getEconomicSector());

        return builder.buildSinglePair(
                        DisbursedInterestArticleType.INTEREST_DEBIT_LEG,
                        DisbursedInterestArticleType.INTEREST_CREDIT_LEG,
                        interestAmount,
                        resolvedAccounts)
                .flatMap(components -> facility.getSanctionedLoan()
                        .map(sanctionedLoan -> documentBuilderService.buildTransaction(
                                facility,
                                sanctionedLoan.getCurrency(),
                                branchCode,
                                components,
                                postTitle,
                                findStrategy(DisbursedInterestArticleType.class, facility),
                                baseMetadata,
                                resolvedAccounts))
                        .orElseGet(() -> Result.failure(Notification.ofError(
                                TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND))));
    }

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
