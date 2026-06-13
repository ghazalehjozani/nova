package ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.SubmitFacilityForApprovalCommand;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.step.SubmitFacilityStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class SubmitFacilityForApprovalCommandHandler
        implements WorkflowCommandHandler<
                SubmitFacilityForApprovalCommand, SubmitFacilityForApprovalCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("submit-facility-for-approval", writePublishing(submitFacilityStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(SubmitFacilityForApprovalCommand command) {
        return branchAccessValidator
                .verifyCallerCoversFacility(command.branchCode(), LoanFacilityId.of(command.loanFacilityId()))
                .map(prepared -> new Data(command, prepared));
    }

    public record Data(SubmitFacilityForApprovalCommand command, Unit prepared) {}

    private final BranchAccessValidator branchAccessValidator;
    private final SubmitFacilityStep submitFacilityStep;
}
