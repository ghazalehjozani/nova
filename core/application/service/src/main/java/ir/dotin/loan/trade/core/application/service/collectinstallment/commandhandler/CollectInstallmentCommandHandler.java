package ir.dotin.loan.trade.core.application.service.collectinstallment.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand;
import ir.dotin.loan.trade.core.application.service.collectinstallment.step.CollectInstallmentStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class CollectInstallmentCommandHandler
        implements WorkflowCommandHandler<CollectInstallmentCommand, CollectInstallmentCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("collect-installment", writePublishing(collectInstallmentStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(CollectInstallmentCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CollectInstallmentCommand command, Unit prepared) {}

    private final CollectInstallmentStep collectInstallmentStep;
}
