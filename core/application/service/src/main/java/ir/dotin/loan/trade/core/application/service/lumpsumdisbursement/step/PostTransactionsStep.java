package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.PostTitle;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.Compensable;
import ir.dotin.platform.pangaea.workflow.api.definition.RemoteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.configuration.LumpSumDisbursementConfiguration;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.PostedTransaction;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.TransactionPostingSupport;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DocumentMetadataType;
import ir.dotin.loan.trade.core.domain.shared.document.transaction.TradeLumpSumDisbursementTransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostTransactionsStep implements RemoteActivity<LumpSumData>, Compensable<LumpSumData> {

    private final FacilityDependencyLoader dependencyLoader;
    private final TransactionPostingSupport transactionPostingSupport;
    private final TradeLumpSumDisbursementTransactionService transactionService;
    private final LumpSumDisbursementConfiguration configuration;
    private final Clock clock;

    public StepResult<Void> execute(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();
        ResolvedAccounts resolvedAccounts = data.getResolvedAccounts();

        var transactionsResult = dependencyLoader
                .loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> dependencyLoader.loadLoanType(facility).flatMap(loanType -> dependencyLoader
                        .loadLoanArrangement(facility)
                        .flatMap(arrangement -> dependencyLoader
                                .loadInstallmentSchedule(facility)
                                .flatMap(schedule -> schedule.activateSchedule(clock)
                                        .flatMap(ignored -> createTransactions(
                                                facility, loanType, arrangement, schedule, data, resolvedAccounts))))));

        if (transactionsResult.isFailure()) {
            return StepResult.failure(transactionsResult.err().orElseThrow());
        }

        var postResult = transactionPostingSupport.postTransactions(
                LoanFacilityId.of(data.facilityId()),
                configuration.getFcbMergedDocumentTitle(),
                transactionsResult.unwrap());

        if (postResult.isFailure()) {
            return StepResult.failure(postResult.err().orElseThrow());
        }

        List<LumpSumData.PostedTransactionData> posted = postResult.unwrap().stream()
                .map(t -> new LumpSumData.PostedTransactionData(t.transactionNumber(), t.trackingId(), t.status()))
                .toList();

        ctx.updateData(d -> d.withPostedTransactions(posted));

        List<String> transactionNumbers = posted.stream()
                .map(LumpSumData.PostedTransactionData::transactionNumber)
                .toList();
        log.info("Transactions posted for facility {}: {}", data.facilityId(), transactionNumbers);
        return new StepResult.Success<>(null);
    }

    public StepResult<Void> compensate(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();
        log.warn("Reversing transactions for facility {}", data.facilityId());

        List<TrackedTransactionNumber> trackedNumbers = rebuildTrackedNumbers(data);
        if (trackedNumbers.isEmpty()) {
            return new StepResult.Success<>(null);
        }

        return StepResult.fromResult(transactionPostingSupport.reverseTransactions(trackedNumbers));
    }

    private List<TrackedTransactionNumber> rebuildTrackedNumbers(LumpSumData data) {
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
            TradeLoanArrangement arrangement,
            InstallmentSchedule schedule,
            LumpSumData data,
            ResolvedAccounts resolvedAccounts) {

        return createBranchCode(data).flatMap(branchCode -> createPostTitle(facility)
                .flatMap(postTitle -> DocumentMetadataUtils.createBaseArticleMetadata(
                                facility,
                                loanType,
                                branchCode,
                                data.transactionConfig(),
                                DocumentMetadataType.DISBURSEMENT)
                        .flatMap(metadata -> transactionService.createTransactions(
                                facility,
                                arrangement,
                                loanType,
                                branchCode,
                                postTitle,
                                metadata,
                                schedule,
                                resolvedAccounts))));
    }

    private Result<BranchCode> createBranchCode(LumpSumData data) {
        return BranchCode.of(data.branchCode());
    }

    private Result<PostTitle> createPostTitle(TradeLoanFacility facility) {
        return PostTitle.of(
                configuration.getPostTitleTemplate().formatted(facility.getId().value()));
    }
}
