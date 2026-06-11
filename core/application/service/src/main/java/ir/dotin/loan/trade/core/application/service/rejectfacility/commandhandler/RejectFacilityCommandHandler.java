package ir.dotin.loan.trade.core.application.service.rejectfacility.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RejectFacilityCommand;
import ir.dotin.loan.trade.core.application.service.rejectfacility.step.RejectFacilityStep;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;

@Service
public final class RejectFacilityCommandHandler
        extends WorkflowCommandHandler<RejectFacilityCommand, RejectFacilityCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        // @formatter:off
        return route.singleWrite("reject-facility", rejectFacilityStep);
        // @formatter:on
    }

    @Override
    protected Result<Data> seed(RejectFacilityCommand command) {
        return branchAccessValidator
                .verifyCallerCoversFacility(command.branchCode(), LoanFacilityId.of(command.loanFacilityId()))
                .map(prepared -> new Data(command, prepared));
    }

    public record Data(RejectFacilityCommand command, Unit prepared) {}

    private final BranchAccessValidator branchAccessValidator;
    private final RejectFacilityStep rejectFacilityStep;

    public RejectFacilityCommandHandler(
            WorkflowEngine engine, BranchAccessValidator branchAccessValidator, RejectFacilityStep rejectFacilityStep) {
        super(engine);
        this.branchAccessValidator = branchAccessValidator;
        this.rejectFacilityStep = rejectFacilityStep;
    }
}
