package ir.dotin.loan.trade.core.domain.loanfacility.service.transaction;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.annotation.DomainService;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.builder.ArticleComponentMapBuilder;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.service.DocumentBuilderService;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.ArticleMetadata;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisburseBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.PaymentAmountArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.i18n.TradeLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeInterestCalculationService;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.DisbursementStrategyProvider;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProviderImpl;

import static java.util.Objects.requireNonNull;

@DomainService
public class TradeLampSunDisbursementTransactionService {

    private final DocumentBuilderService documentBuilderService;
    private final DisbursementStrategyProvider strategyProvider;
    private final TradeInterestCalculationService tradeInterestCalculationService;

    public TradeLampSunDisbursementTransactionService(
            DocumentBuilderService documentBuilderService,
            DisbursementStrategyProvider strategyProvider,
            TradeInterestCalculationService tradeInterestCalculationService) {
        this.documentBuilderService = requireNonNull(documentBuilderService);
        this.strategyProvider = requireNonNull(strategyProvider);
        this.tradeInterestCalculationService = requireNonNull(tradeInterestCalculationService);
    }

    public Result<List<LoanTransaction>> createTransactions(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanArrangement loanArrangement,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            @NonNull InstallmentSchedule installmentSchedule) {

        return facility.getSanctionedLoan()
                .map(sanctionedLoan -> buildTransactions(
                        facility,
                        loanArrangement,
                        sanctionedLoan,
                        loanType,
                        branchCode,
                        postTitle,
                        baseMetadata,
                        installmentSchedule))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND_FOR_FACILITY,
                        facility.getId().value())));
    }

    public Result<LoanTransaction> createBankCommitmentTransaction(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull Money amount,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata) {

        ArticleComponentMapBuilder<TradeRelationType> builder = new ArticleComponentMapBuilder<>(
                loanType.getRelationTypeLoanTopics(),
                requireNonNull(facility.getLoanApplication()).getEconomicSector());

        return builder.buildSinglePair(
                        DisburseBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG,
                        DisburseBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG,
                        amount)
                .flatMap(components -> facility.getSanctionedLoan()
                        .map(sanctionedLoan -> documentBuilderService.buildTransaction(
                                facility,
                                sanctionedLoan.getCurrency(),
                                branchCode,
                                components,
                                postTitle,
                                findStrategy(DisburseBankCommitmentArticleType.class, facility),
                                baseMetadata))
                        .orElseGet(() -> Result.failure(Notification.ofError(
                                TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND_FOR_FACILITY))));
    }

    public Result<LoanTransaction> createPaymentAmountTransaction(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull Money amount,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata) {

        ArticleComponentMapBuilder<TradeRelationType> builder = new ArticleComponentMapBuilder<>(
                loanType.getRelationTypeLoanTopics(),
                requireNonNull(facility.getLoanApplication()).getEconomicSector());

        return builder.buildSinglePairWithContext(
                        PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG,
                        PaymentAmountArticleType.DISBURSEMENT_CREDIT,
                        amount,
                        facility.getLoanApplication().getDisburseDestination())
                .flatMap(components -> facility.getSanctionedLoan()
                        .map(sanctionedLoan -> documentBuilderService.buildTransaction(
                                facility,
                                sanctionedLoan.getCurrency(),
                                branchCode,
                                components,
                                postTitle,
                                findStrategy(PaymentAmountArticleType.class, facility),
                                baseMetadata))
                        .orElseGet(() -> Result.failure(Notification.ofError(
                                TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND))));
    }

    public Result<LoanTransaction> createDisbursedInterestTransaction(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull Money interestAmount,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata) {

        ArticleComponentMapBuilder<TradeRelationType> builder = new ArticleComponentMapBuilder<>(
                loanType.getRelationTypeLoanTopics(),
                facility.getLoanApplication().getEconomicSector());

        return builder.buildSinglePair(
                        DisbursedInterestArticleType.INTEREST_DEBIT_LEG,
                        DisbursedInterestArticleType.INTEREST_CREDIT_LEG,
                        interestAmount)
                .flatMap(components -> facility.getSanctionedLoan()
                        .map(sanctionedLoan -> documentBuilderService.buildTransaction(
                                facility,
                                sanctionedLoan.getCurrency(),
                                branchCode,
                                components,
                                postTitle,
                                findStrategy(DisbursedInterestArticleType.class, facility),
                                baseMetadata))
                        .orElseGet(() -> Result.failure(Notification.ofError(
                                TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND))));
    }

    private Result<List<LoanTransaction>> buildTransactions(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanArrangement tradeLoanArrangement,
            @NonNull TradeSanctionedLoan sanctionedLoan,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            @NonNull InstallmentSchedule installmentSchedule) {

        Money interestAmount = calculateInterestAmount(facility, tradeLoanArrangement, installmentSchedule);
        Money approvedAmount = sanctionedLoan.getApprovedAmount();

        List<Result<LoanTransaction>> transactionResults = ImmutableList.of(
                createBankCommitmentTransaction(
                        facility, loanType, branchCode, requireNonNull(approvedAmount), postTitle, baseMetadata),
                createPaymentAmountTransaction(facility, loanType, branchCode, approvedAmount, postTitle, baseMetadata),
                createDisbursedInterestTransaction(
                        facility, loanType, branchCode, interestAmount, postTitle, baseMetadata));

        return Result.traverse(transactionResults, result -> result);
    }

    private Money calculateInterestAmount(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeLoanArrangement arrangement,
            @NonNull InstallmentSchedule installmentSchedule) {

        InstallmentPaymentType paymentType = arrangement.getInstallmentPolicy().installmentPaymentType();

        if (paymentType == InstallmentPaymentType.GRADUAL) {
            return installmentSchedule.getInstallments().stream()
                    .map(installment -> installment.getScheduledAmount().interestAmount())
                    .reduce((money, other) -> money.add(other).getValue())
                    .orElseThrow(() ->
                            new IllegalStateException("No installments found in schedule for GRADUAL payment type"));
        } else {
            TradeLoanParameterProviderImpl tradeLoanParameterProvider = TradeLoanParameterProviderImpl.of(facility);
            return tradeInterestCalculationService
                    .calculateInterest(arrangement.getInterestPolicy(), tradeLoanParameterProvider)
                    .orElseThrow();
        }
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
