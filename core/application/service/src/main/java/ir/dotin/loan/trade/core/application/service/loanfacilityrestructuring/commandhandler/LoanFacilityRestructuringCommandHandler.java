package ir.dotin.loan.trade.core.application.service.loanfacilityrestructuring.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LoanFacilityRestructuringCommand;
import ir.dotin.loan.trade.core.application.service.loanfacilityrestructuring.step.RestructureFacilityStep;

@Service
public final class LoanFacilityRestructuringCommandHandler
        extends WorkflowCommandHandler<LoanFacilityRestructuringCommand, LoanFacilityRestructuringCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        // @formatter:off
        return route.singleWrite("loan-facility-restructuring", restructureFacilityStep);
        // @formatter:on
    }

    @Override
    protected Result<Data> seed(LoanFacilityRestructuringCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(LoanFacilityRestructuringCommand command, Unit prepared) {}

    private final RestructureFacilityStep restructureFacilityStep;

    public LoanFacilityRestructuringCommandHandler(
            WorkflowEngine engine, RestructureFacilityStep restructureFacilityStep) {
        super(engine);
        this.restructureFacilityStep = restructureFacilityStep;
    }
}
