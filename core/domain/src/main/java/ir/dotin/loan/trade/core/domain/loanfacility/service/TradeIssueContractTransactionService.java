package ir.dotin.loan.trade.core.domain.loanfacility.service;

import java.util.Map;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.NotificationError;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.builder.ArticleComponentMapBuilder;
import ir.dotin.loan.baseloan.core.domain.shared.service.DocumentBuilderService;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleComponent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.ArticleMetadata;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.IssueContractBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.i18n.TradeLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.IssueContractCommitmentHandlingStrategy;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

@DomainService
public class TradeIssueContractTransactionService {

    private final DocumentBuilderService documentBuilderService;
    private final IssueContractCommitmentHandlingStrategy strategy;

    public TradeIssueContractTransactionService(
            DocumentBuilderService documentBuilderService, IssueContractCommitmentHandlingStrategy strategy) {
        this.documentBuilderService = requireNonNull(documentBuilderService);
        this.strategy = requireNonNull(strategy);
    }

    public Result<LoanTransaction> createIssueContractTransaction(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            BranchCode branchCode,
            PostTitle postTitle,
            ArticleMetadata baseMetadata) {

        return validateInputs(facility, loanType, branchCode, postTitle).flatMap(ignored -> facility.getSanctionedLoan()
                .map(sanctionedLoan ->
                        buildTransaction(facility, sanctionedLoan, loanType, branchCode, postTitle, baseMetadata))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanFacilityLocalizedMessageCodes.SANCTIONED_LOAN_NOT_FOUND_FOR_FACILITY,
                        facility.getId().value()))));
    }

    private Result<LoanTransaction> buildTransaction(
            TradeLoanFacility facility,
            TradeSanctionedLoan sanctionedLoan,
            TradeLoanType loanType,
            BranchCode branchCode,
            PostTitle postTitle,
            ArticleMetadata baseMetadata) {

        return buildArticleComponents(facility, sanctionedLoan, loanType)
                .flatMap(components -> documentBuilderService.buildTransaction(
                        facility,
                        sanctionedLoan.getCurrency(),
                        branchCode,
                        components,
                        postTitle,
                        strategy,
                        baseMetadata));
    }

    private Result<Map<IssueContractBankCommitmentArticleType, ArticleComponent>> buildArticleComponents(
            TradeLoanFacility facility, TradeSanctionedLoan sanctionedLoan, TradeLoanType loanType) {

        ArticleComponentMapBuilder<TradeRelationType> builder = new ArticleComponentMapBuilder<>(
                loanType.getRelationTypeLoanTopics(),
                facility.getLoanApplication().getEconomicSector());

        return builder.buildSinglePair(
                IssueContractBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG,
                IssueContractBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG,
                sanctionedLoan.getApprovedAmount());
    }

    private Result<Void> validateInputs(
            TradeLoanFacility facility, TradeLoanType loanType, BranchCode branchCode, PostTitle postTitle) {

        if (facility == null) {
            return Result.failure(Notification.ofError(
                    NotificationError.of(TradeLoanFacilityLocalizedMessageCodes.FACILITY_CANNOT_BE_NULL)));
        }

        if (loanType == null) {
            return Result.failure(Notification.ofError(
                    NotificationError.of(TradeLoanFacilityLocalizedMessageCodes.LOAN_TYPE_CANNOT_BE_NULL)));
        }

        if (branchCode == null) {
            return Result.failure(Notification.ofError(
                    NotificationError.of(TradeLoanFacilityLocalizedMessageCodes.BRANCH_CODE_CANNOT_BE_NULL)));
        }

        if (postTitle == null) {
            return Result.failure(Notification.ofError(
                    NotificationError.of(TradeLoanFacilityLocalizedMessageCodes.POST_TITLE_CANNOT_BE_NULL)));
        }

        return Result.success();
    }
}
