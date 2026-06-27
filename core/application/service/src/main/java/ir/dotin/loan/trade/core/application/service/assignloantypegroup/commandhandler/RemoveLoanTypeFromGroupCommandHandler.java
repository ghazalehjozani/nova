package ir.dotin.loan.trade.core.application.service.assignloantypegroup.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RemoveLoanTypeFromGroupCommand;
import ir.dotin.loan.trade.core.application.service.assignloantypegroup.data.RemoveLoanTypeFromGroupData;
import ir.dotin.loan.trade.core.application.service.assignloantypegroup.step.RemoveLoanTypeFromGroupStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class RemoveLoanTypeFromGroupCommandHandler
        implements WorkflowCommandHandler<RemoveLoanTypeFromGroupCommand, RemoveLoanTypeFromGroupData> {

    @Override
    public Workflow<RemoveLoanTypeFromGroupData> definition() {
        return Workflow.singleWrite("remove-loan-type-from-group", writePublishing(step));
    }

    @Override
    public Result<RemoveLoanTypeFromGroupData> seed(RemoveLoanTypeFromGroupCommand command) {
        return Result.success(new RemoveLoanTypeFromGroupData(command));
    }

    private final RemoveLoanTypeFromGroupStep step;
}
