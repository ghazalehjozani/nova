package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.Compensable;
import ir.dotin.platform.pangaea.workflow.api.definition.RemoteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.AccountResolutionSupport;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResolveAccountsStep implements RemoteActivity<LumpSumData>, Compensable<LumpSumData> {

    private final FacilityDependencyLoader dependencyLoader;
    private final AccountResolutionSupport accountResolutionSupport;

    public StepResult<Void> execute(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();

        var result = dependencyLoader
                .loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> dependencyLoader.loadLoanType(facility).flatMap(loanType -> dependencyLoader
                        .loadLoanArrangement(facility)
                        .flatMap(arrangement -> accountResolutionSupport.resolveAccounts(
                                facility,
                                loanType,
                                arrangement.getCurrencyType().getCode()))));

        if (result.isFailure()) {
            return StepResult.failure(result.err().orElseThrow());
        }

        ctx.updateData(d -> d.withResolvedAccounts(result.unwrap()));

        return new StepResult.Success<>(null);
    }

    public StepResult<Void> compensate(WorkflowContext<LumpSumData> ctx) {
        accountResolutionSupport.closeAccounts(ctx.data().resolvedAccounts());
        return new StepResult.Success<>(null);
    }
}
