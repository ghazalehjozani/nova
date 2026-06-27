package ir.dotin.loan.trade.core.application.service.loantypegroup.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RenameLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.RenameLoanTypeGroupData;
import ir.dotin.loan.trade.core.application.service.loantypegroup.step.RenameLoanTypeGroupStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class RenameLoanTypeGroupCommandHandler
        implements WorkflowCommandHandler<RenameLoanTypeGroupCommand, RenameLoanTypeGroupData> {

    @Override
    public Workflow<RenameLoanTypeGroupData> definition() {
        return Workflow.singleWrite("rename-loan-type-group", writePublishing(step));
    }

    @Override
    public Result<RenameLoanTypeGroupData> seed(RenameLoanTypeGroupCommand command) {
        return Result.success(new RenameLoanTypeGroupData(command));
    }

    private final RenameLoanTypeGroupStep step;
}
