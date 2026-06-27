package ir.dotin.loan.trade.core.application.service.loantypegroup.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.MoveLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.MoveLoanTypeGroupData;
import ir.dotin.loan.trade.core.application.service.loantypegroup.step.MoveLoanTypeGroupStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class MoveLoanTypeGroupCommandHandler
        implements WorkflowCommandHandler<MoveLoanTypeGroupCommand, MoveLoanTypeGroupData> {

    @Override
    public Workflow<MoveLoanTypeGroupData> definition() {
        return Workflow.singleWrite("move-loan-type-group", writePublishing(step));
    }

    @Override
    public Result<MoveLoanTypeGroupData> seed(MoveLoanTypeGroupCommand command) {
        return Result.success(new MoveLoanTypeGroupData(command));
    }

    private final MoveLoanTypeGroupStep step;
}
