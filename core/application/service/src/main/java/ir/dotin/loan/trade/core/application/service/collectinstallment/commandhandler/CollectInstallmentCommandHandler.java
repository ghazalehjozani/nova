package ir.dotin.loan.trade.core.application.service.collectinstallment.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand;
import ir.dotin.loan.trade.core.application.service.collectinstallment.step.CollectInstallmentStep;

@Service
public final class CollectInstallmentCommandHandler
        extends WorkflowCommandHandler<CollectInstallmentCommand, CollectInstallmentCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        // @formatter:off
        return route.singleWrite("collect-installment", collectInstallmentStep);
        // @formatter:on
    }

    @Override
    protected Result<Data> seed(CollectInstallmentCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CollectInstallmentCommand command, Unit prepared) {}

    private final CollectInstallmentStep collectInstallmentStep;

    public CollectInstallmentCommandHandler(WorkflowEngine engine, CollectInstallmentStep collectInstallmentStep) {
        super(engine);
        this.collectInstallmentStep = collectInstallmentStep;
    }
}
