package ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityPaidOffCommand;
import ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.step.ClosePaidOffFacilityStep;

@Service
public final class CloseFacilityPaidOffCommandHandler
        extends WorkflowCommandHandler<CloseFacilityPaidOffCommand, CloseFacilityPaidOffCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        // @formatter:off
        return route.singleWrite("close-facility-paid-off", closePaidOffFacilityStep);
        // @formatter:on
    }

    @Override
    protected Result<Data> seed(CloseFacilityPaidOffCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CloseFacilityPaidOffCommand command, Unit prepared) {}

    private final ClosePaidOffFacilityStep closePaidOffFacilityStep;

    public CloseFacilityPaidOffCommandHandler(
            WorkflowEngine engine, ClosePaidOffFacilityStep closePaidOffFacilityStep) {
        super(engine);
        this.closePaidOffFacilityStep = closePaidOffFacilityStep;
    }
}
