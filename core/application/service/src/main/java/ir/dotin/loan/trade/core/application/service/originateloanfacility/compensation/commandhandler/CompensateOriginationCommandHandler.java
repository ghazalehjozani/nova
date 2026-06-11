package ir.dotin.loan.trade.core.application.service.originateloanfacility.compensation.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateOriginationCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.compensation.step.RevertOriginationData;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.compensation.step.RevertOriginationStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
final class CompensateOriginationCommandHandler
        extends WorkflowCommandHandler<CompensateOriginationCommand, RevertOriginationData> {

    @Override
    protected Workflow<RevertOriginationData> route(WorkflowRoute<RevertOriginationData> route) {
        // @formatter:off
        return route.singleWrite("compensate-origination", revertOriginationStep);
        // @formatter:on
    }

    @Override
    protected Result<RevertOriginationData> seed(CompensateOriginationCommand command) {
        log.warn("Compensating origination for facility: {}", command.loanFacilityId());
        return Result.success(new RevertOriginationData(command, Unit.INSTANCE));
    }

    private final RevertOriginationStep revertOriginationStep;

    CompensateOriginationCommandHandler(WorkflowEngine engine, RevertOriginationStep revertOriginationStep) {
        super(engine);
        this.revertOriginationStep = revertOriginationStep;
    }
}
