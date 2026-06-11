package ir.dotin.loan.trade.core.application.service.collectinstallment.compensation.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollectInstallmentCommand;
import ir.dotin.loan.trade.core.application.service.collectinstallment.compensation.step.RevertInstallmentCollectionStep;

@Service
public final class CompensateCollectInstallmentCommandHandler
        extends WorkflowCommandHandler<
                CompensateCollectInstallmentCommand, CompensateCollectInstallmentCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        // @formatter:off
        return route.singleWrite("compensate-collect-installment", revertInstallmentCollectionStep);
        // @formatter:on
    }

    @Override
    protected Result<Data> seed(CompensateCollectInstallmentCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CompensateCollectInstallmentCommand command, Unit prepared) {}

    private final RevertInstallmentCollectionStep revertInstallmentCollectionStep;

    public CompensateCollectInstallmentCommandHandler(
            WorkflowEngine engine, RevertInstallmentCollectionStep revertInstallmentCollectionStep) {
        super(engine);
        this.revertInstallmentCollectionStep = revertInstallmentCollectionStep;
    }
}
