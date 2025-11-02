package ir.dotin.loan.trade.core.domain.loanfacility.service;

import java.util.List;

import com.google.common.collect.ImmutableList;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.annotation.DomainService;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.builder.ArticleComponentMapBuilder;
import ir.dotin.loan.baseloan.core.domain.shared.service.DocumentBuilderService;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.PostTitle;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisburseBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.PaymentAmountArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.i18n.TradeLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.DisbursementStrategyProvider;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

@DomainService
public class TradeDisbursementTransactionService {

    private final DocumentBuilderService documentBuilderService;
    private final DisbursementStrategyProvider strategyProvider;
    private final TradeInterestCalculationService tradeInterestCalculationService;

    public TradeDisbursementTransactionService(
            DocumentBuilderService documentBuilderService,
            DisbursementStrategyProvider strategyProvider,
            TradeInterestCalculationService tradeInterestCalculationService) {
        this.documentBuilderService = requireNonNull(documentBuilderService);
        this.strategyProvider = requireNonNull(strategyProvider);
        this.tradeInterestCalculationService = tradeInterestCalculationService;
    }

    public Result<List<LoanTransaction>> createDisbursementTransactions(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            BranchCode branchCode,
            Money disbursementAmount,
            PostTitle postTitle) {

        return validateDisbursementInputs(facility, loanType, branchCode, disbursementAmount, postTitle)
                .flatMap(ignored -> facility.getSanctionedLoan()
                        .map(sanctionedLoan -> buildTransactions(
                                facility, sanctionedLoan, loanType, branchCode, disbursementAmount, postTitle))
                        .orElseGet(() -> Result.failure(Notification.ofError(
                                TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND_FOR_FACILITY,
                                facility.getId().value()))));
    }

    public Result<LoanTransaction> createBankCommitmentTransaction(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            BranchCode branchCode,
            Money amount,
            PostTitle postTitle) {

        ArticleComponentMapBuilder<TradeRelationType> builder = new ArticleComponentMapBuilder<>(
                loanType.getRelationTypeLoanTopics(),
                facility.getLoanApplication().getEconomicSector());

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
                                findStrategy(DisburseBankCommitmentArticleType.class),
                                null)) // TODO: create base in app service
                        .orElseGet(() -> Result.failure(Notification.ofError(
                                TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND_FOR_FACILITY))));
    }

    public Result<LoanTransaction> createPaymentAmountTransaction(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            BranchCode branchCode,
            Money amount,
            PostTitle postTitle) {

        ArticleComponentMapBuilder<TradeRelationType> builder = new ArticleComponentMapBuilder<>(
                loanType.getRelationTypeLoanTopics(),
                facility.getLoanApplication().getEconomicSector());

        return builder.buildSinglePair(
                        PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG,
                        PaymentAmountArticleType.DISBURSEMENT_CREDIT,
                        amount)
                .flatMap(components -> facility.getSanctionedLoan()
                        .map(sanctionedLoan -> documentBuilderService.buildTransaction(
                                facility,
                                sanctionedLoan.getCurrency(),
                                branchCode,
                                components,
                                postTitle,
                                findStrategy(PaymentAmountArticleType.class),
                                null)) // TODO: create base in app service
                        .orElseGet(() -> Result.failure(Notification.ofError(
                                TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND))));
    }

    public Result<LoanTransaction> createDisbursedInterestTransaction(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            BranchCode branchCode,
            Money interestAmount,
            PostTitle postTitle) {

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
                                findStrategy(DisbursedInterestArticleType.class),
                                null)) // TODO: create base in app service
                        .orElseGet(() -> Result.failure(Notification.ofError(
                                TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND))));
    }

    private Result<List<LoanTransaction>> buildTransactions(
            TradeLoanFacility facility,
            TradeSanctionedLoan sanctionedLoan,
            TradeLoanType loanType,
            BranchCode branchCode,
            Money disbursementAmount,
            PostTitle postTitle) {

        List<Result<LoanTransaction>> transactionResults = ImmutableList.of(
                createBankCommitmentTransaction(facility, loanType, branchCode, disbursementAmount, postTitle),
                createPaymentAmountTransaction(facility, loanType, branchCode, disbursementAmount, postTitle)
                //                createDisbursedInterestTransaction(facility, loanType, branchCode, interestAmount,
                // postTitle)
                );

        return Result.traverse(transactionResults, result -> result);
    }

    @SuppressWarnings("unchecked")
    private <K extends Enum<K> & ArticleType<K, TradeRelationType>>
            DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, K> findStrategy(
                    Class<K> articleTypeClass) {
        List<
                        DocumentCalculationStrategy<
                                TradeLoanFacility, TradeRelationType, ? extends ArticleType<?, TradeRelationType>>>
                strategies = strategyProvider.getStrategies(null);

        for (var strategy : strategies) {
            // This is a simplified version - you might need more sophisticated type checking
            try {
                return (DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, K>) strategy;
            } catch (ClassCastException e) {
                // Continue to next strategy
            }
        }

        throw new IllegalStateException("No strategy found for article type: " + articleTypeClass);
    }

    private Result<Void> validateDisbursementInputs(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            BranchCode branchCode,
            Money disbursementAmount,
            PostTitle postTitle) {

        if (facility == null
                || loanType == null
                || branchCode == null
                || disbursementAmount == null
                || postTitle == null) {
            return Result.failure(
                    Notification.ofError(TradeLoanFacilityLocalizedMessageCodes.ALL_DISBURSEMENT_INPUTS_REQUIRED));
        }

        return Result.success();
    }
}
