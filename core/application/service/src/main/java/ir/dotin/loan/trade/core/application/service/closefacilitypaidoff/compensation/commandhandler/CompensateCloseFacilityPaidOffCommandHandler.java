package ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.compensation.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCloseFacilityPaidOffCommand;
import ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.compensation.step.RevertClosePaidOffStep;

@Service
public final class CompensateCloseFacilityPaidOffCommandHandler
        extends WorkflowCommandHandler<
                CompensateCloseFacilityPaidOffCommand, CompensateCloseFacilityPaidOffCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        // @formatter:off
        return route.singleWrite("compensate-close-facility-paid-off", revertClosePaidOffStep);
        // @formatter:on
    }

    @Override
    protected Result<Data> seed(CompensateCloseFacilityPaidOffCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CompensateCloseFacilityPaidOffCommand command, Unit prepared) {}

    private final RevertClosePaidOffStep revertClosePaidOffStep;

    public CompensateCloseFacilityPaidOffCommandHandler(
            WorkflowEngine engine, RevertClosePaidOffStep revertClosePaidOffStep) {
        super(engine);
        this.revertClosePaidOffStep = revertClosePaidOffStep;
    }
}
