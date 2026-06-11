package ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.SubmitFacilityForApprovalCommand;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.step.SubmitFacilityStep;

@Service
public final class SubmitFacilityForApprovalCommandHandler
        extends WorkflowCommandHandler<SubmitFacilityForApprovalCommand, SubmitFacilityForApprovalCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        // @formatter:off
        return route.singleWrite("submit-facility-for-approval", submitFacilityStep);
        // @formatter:on
    }

    @Override
    protected Result<Data> seed(SubmitFacilityForApprovalCommand command) {
        return branchAccessValidator
                .verifyCallerCoversFacility(command.branchCode(), LoanFacilityId.of(command.loanFacilityId()))
                .map(prepared -> new Data(command, prepared));
    }

    public record Data(SubmitFacilityForApprovalCommand command, Unit prepared) {}

    private final BranchAccessValidator branchAccessValidator;
    private final SubmitFacilityStep submitFacilityStep;

    public SubmitFacilityForApprovalCommandHandler(
            WorkflowEngine engine, BranchAccessValidator branchAccessValidator, SubmitFacilityStep submitFacilityStep) {
        super(engine);
        this.branchAccessValidator = branchAccessValidator;
        this.submitFacilityStep = submitFacilityStep;
    }
}
