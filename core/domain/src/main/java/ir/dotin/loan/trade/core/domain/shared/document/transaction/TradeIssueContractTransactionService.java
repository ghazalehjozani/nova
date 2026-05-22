package ir.dotin.loan.trade.core.domain.shared.document.transaction;

import java.time.Clock;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.accounting.document.api.model.ArticleComponent;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.PostTitle;
import ir.dotin.platform.accounting.document.api.model.metadata.ArticleMetadata;
import ir.dotin.platform.accounting.document.core.assembly.DocumentAssembler;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loanfacility.error.TradeLoanFacilityErrors;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.shared.document.builder.TradeArticleComponentBuilder;
import ir.dotin.loan.trade.core.domain.shared.document.enums.IssueContractBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.IssueContractCommitmentHandlingStrategy;

import static java.util.Objects.requireNonNull;

@DomainService
public class TradeIssueContractTransactionService {

    private final DocumentAssembler documentAssembler;
    private final IssueContractCommitmentHandlingStrategy strategy;
    private final Clock clock;

    public TradeIssueContractTransactionService(
            DocumentAssembler documentAssembler, IssueContractCommitmentHandlingStrategy strategy, Clock clock) {
        this.documentAssembler = requireNonNull(documentAssembler);
        this.strategy = requireNonNull(strategy);
        this.clock = requireNonNull(clock);
    }

    public Result<LoanTransaction> createIssueContractTransaction(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            BranchCode branchCode,
            PostTitle postTitle,
            ArticleMetadata baseMetadata,
            ResolvedAccounts resolvedAccounts) {

        return facility.getSanctionedLoan()
                .map(sanctionedLoan -> buildTransaction(
                        facility, sanctionedLoan, loanType, branchCode, postTitle, baseMetadata, resolvedAccounts))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanFacilityErrors.SANCTIONED_LOAN_NOT_FOUND_FOR_FACILITY,
                        facility.getId().value())));
    }

    private Result<LoanTransaction> buildTransaction(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeSanctionedLoan sanctionedLoan,
            @NonNull TradeLoanType loanType,
            @NonNull BranchCode branchCode,
            @NonNull PostTitle postTitle,
            @NonNull ArticleMetadata baseMetadata,
            ResolvedAccounts resolvedAccounts) {

        return buildArticleComponents(facility, sanctionedLoan, loanType, resolvedAccounts)
                .flatMap(components -> documentAssembler.assemble(
                        facility,
                        sanctionedLoan.getCurrency(),
                        branchCode,
                        components,
                        postTitle,
                        strategy,
                        baseMetadata,
                        resolvedAccounts::getAccount))
                .flatMap(document -> LoanTransaction.of(facility.getId(), document, clock));
    }

    private Result<Map<IssueContractBankCommitmentArticleType, ArticleComponent>> buildArticleComponents(
            @NonNull TradeLoanFacility facility,
            @NonNull TradeSanctionedLoan sanctionedLoan,
            @NonNull TradeLoanType loanType,
            ResolvedAccounts resolvedAccounts) {

        var builder = new TradeArticleComponentBuilder(
                loanType.getRelationTypeLoanTopics(),
                requireNonNull(facility.getLoanApplication()).getEconomicSector());

        return builder.buildSinglePair(
                IssueContractBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG,
                IssueContractBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG,
                sanctionedLoan.getApprovedAmount(),
                resolvedAccounts);
    }
}
