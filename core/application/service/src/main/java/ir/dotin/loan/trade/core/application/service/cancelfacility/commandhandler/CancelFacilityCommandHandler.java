package ir.dotin.loan.trade.core.application.service.cancelfacility.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CancelFacilityCommand;
import ir.dotin.loan.trade.core.application.service.cancelfacility.step.CancelFacilityStep;

@Service
public final class CancelFacilityCommandHandler
        extends WorkflowCommandHandler<CancelFacilityCommand, CancelFacilityCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        // @formatter:off
        return route.singleWrite("cancel-facility", cancelFacilityStep);
        // @formatter:on
    }

    @Override
    protected Result<Data> seed(CancelFacilityCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CancelFacilityCommand command, Unit prepared) {}

    private final CancelFacilityStep cancelFacilityStep;

    public CancelFacilityCommandHandler(WorkflowEngine engine, CancelFacilityStep cancelFacilityStep) {
        super(engine);
        this.cancelFacilityStep = cancelFacilityStep;
    }
}
