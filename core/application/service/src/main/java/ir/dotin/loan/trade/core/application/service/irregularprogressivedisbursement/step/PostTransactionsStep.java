package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.PostTitle;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.Compensable;
import ir.dotin.platform.pangaea.workflow.api.definition.RemoteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.configuration.IrregularProgressiveDisbursementConfiguration;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData.PostedTransactionData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.PostedTransaction;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.TransactionPostingSupport;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DocumentMetadataType;
import ir.dotin.loan.trade.core.domain.shared.document.transaction.IrregularProgressiveDisbursementTransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostTransactionsStep
        implements RemoteActivity<IrregularDisbursementData>, Compensable<IrregularDisbursementData> {

    private final FacilityDependencyLoader dependencyLoader;
    private final TransactionPostingSupport transactionPostingSupport;
    private final IrregularPlanRecalculator planRecalculator;
    private final IrregularProgressiveDisbursementTransactionService transactionService;
    private final IrregularProgressiveDisbursementConfiguration configuration;

    public StepResult<Void> execute(WorkflowContext<IrregularDisbursementData> ctx) {
        var data = ctx.data();

        var transactionsResult = dependencyLoader
                .loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> dependencyLoader.loadLoanType(facility).flatMap(loanType -> dependencyLoader
                        .loadInstallmentSchedule(facility)
                        .flatMap(schedule -> planRecalculator
                                .recalculateAndVerify(facility, schedule, data)
                                .flatMap(installments ->
                                        createTransactions(facility, loanType, schedule, installments, data)))));

        if (transactionsResult.isFailure()) {
            return StepResult.failure(transactionsResult.err().orElseThrow());
        }

        List<LoanTransaction> transactions = transactionsResult.unwrap();

        var postResult = transactionPostingSupport.postTransactions(
                LoanFacilityId.of(data.facilityId()), configuration.getFcbMergedDocumentTitle(), transactions);

        if (postResult.isFailure()) {
            return StepResult.failure(postResult.err().orElseThrow());
        }

        List<PostedTransactionData> posted = postResult.unwrap().stream()
                .map(t -> new PostedTransactionData(t.transactionNumber(), t.trackingId(), t.status()))
                .toList();

        Map<String, String> accountIds = extractAccountIds(transactions);

        ctx.updateData(d -> d.withPostedTransactions(posted, accountIds));

        List<String> transactionNumbers =
                posted.stream().map(PostedTransactionData::transactionNumber).toList();
        log.info("Transactions posted for facility {}: {}", data.facilityId(), transactionNumbers);
        return new StepResult.Success<>(null);
    }

    public StepResult<Void> compensate(WorkflowContext<IrregularDisbursementData> ctx) {
        var data = ctx.data();
        log.warn("Reversing transactions for facility {}", data.facilityId());

        List<TrackedTransactionNumber> trackedNumbers = rebuildTrackedNumbers(data);
        if (trackedNumbers.isEmpty()) {
            return new StepResult.Success<>(null);
        }

        return StepResult.fromResult(transactionPostingSupport.reverseTransactions(trackedNumbers));
    }

    private List<TrackedTransactionNumber> rebuildTrackedNumbers(IrregularDisbursementData data) {
        if (data.postedTransactions() == null) {
            return List.of();
        }
        List<PostedTransaction> posted = data.postedTransactions().stream()
                .map(p -> new PostedTransaction(p.transactionNumber(), p.trackingId(), p.status()))
                .toList();
        return transactionPostingSupport.rebuildTrackedNumbers(posted);
    }

    private Result<List<LoanTransaction>> createTransactions(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            InstallmentSchedule schedule,
            List<Installment> recalculatedInstallments,
            IrregularDisbursementData data) {

        return createBranchCode(data).flatMap(branchCode -> createPostTitle(facility, data)
                .flatMap(postTitle -> DocumentMetadataUtils.createBaseArticleMetadata(
                                facility,
                                loanType,
                                branchCode,
                                data.transactionConfig(),
                                DocumentMetadataType.DISBURSEMENT)
                        .flatMap(metadata -> transactionService.createTransactions(
                                facility,
                                loanType,
                                branchCode,
                                postTitle,
                                metadata,
                                schedule,
                                recalculatedInstallments,
                                planRecalculator.trancheMoney(data),
                                data.getResolvedAccounts()))));
    }

    private Map<String, String> extractAccountIds(List<LoanTransaction> transactions) {
        Map<RelationType<?>, AccountId> allAccountIds = new LinkedHashMap<>();
        transactions.stream()
                .flatMap(tx -> tx.extractAccountIdsByRelationType().entrySet().stream())
                .forEach(entry -> allAccountIds.merge(entry.getKey(), entry.getValue(), (existing, newValue) -> {
                    if (!existing.equals(newValue)) {
                        throw new IllegalStateException(
                                "Conflicting account ID for relation type: %s, existing: %s, new: %s"
                                        .formatted(entry.getKey(), existing.value(), newValue.value()));
                    }
                    return existing;
                }));
        return allAccountIds.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(), e -> e.getValue().value()));
    }

    private Result<BranchCode> createBranchCode(IrregularDisbursementData data) {
        return BranchCode.of(data.branchCode());
    }

    private Result<PostTitle> createPostTitle(TradeLoanFacility facility, IrregularDisbursementData data) {
        String title =
                configuration.getPostTitleTemplate().formatted(facility.getId().value(), data.trancheNumber());
        return PostTitle.of(title);
    }
}
