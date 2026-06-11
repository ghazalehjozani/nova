package ir.dotin.loan.trade.core.application.service.closefacilitydefaulted.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityDefaultedCommand;
import ir.dotin.loan.trade.core.application.service.closefacilitydefaulted.step.CloseDefaultedFacilityStep;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;

@Service
public final class CloseFacilityDefaultedCommandHandler
        extends WorkflowCommandHandler<CloseFacilityDefaultedCommand, CloseFacilityDefaultedCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        // @formatter:off
        return route.singleWrite("close-facility-defaulted", closeDefaultedFacilityStep);
        // @formatter:on
    }

    @Override
    protected Result<Data> seed(CloseFacilityDefaultedCommand command) {
        return branchAccessValidator
                .verifyCallerCoversFacility(command.branchCode(), LoanFacilityId.of(command.loanFacilityId()))
                .map(prepared -> new Data(command, prepared));
    }

    public record Data(CloseFacilityDefaultedCommand command, Unit prepared) {}

    private final BranchAccessValidator branchAccessValidator;
    private final CloseDefaultedFacilityStep closeDefaultedFacilityStep;

    public CloseFacilityDefaultedCommandHandler(
            WorkflowEngine engine,
            BranchAccessValidator branchAccessValidator,
            CloseDefaultedFacilityStep closeDefaultedFacilityStep) {
        super(engine);
        this.branchAccessValidator = branchAccessValidator;
        this.closeDefaultedFacilityStep = closeDefaultedFacilityStep;
    }
}
