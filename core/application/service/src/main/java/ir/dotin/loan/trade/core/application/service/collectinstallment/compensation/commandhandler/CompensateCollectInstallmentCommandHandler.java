package ir.dotin.loan.trade.core.application.service.collectinstallment.compensation.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollectInstallmentCommand;
import ir.dotin.loan.trade.core.application.service.collectinstallment.compensation.step.RevertInstallmentCollectionStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class CompensateCollectInstallmentCommandHandler
        implements WorkflowCommandHandler<
                CompensateCollectInstallmentCommand, CompensateCollectInstallmentCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("compensate-collect-installment", writePublishing(revertInstallmentCollectionStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(CompensateCollectInstallmentCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CompensateCollectInstallmentCommand command, Unit prepared) {}

    private final RevertInstallmentCollectionStep revertInstallmentCollectionStep;
}
