package ir.dotin.loan.trade.core.application.service.loantypegroup.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CreateLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.CreateLoanTypeGroupData;
import ir.dotin.loan.trade.core.application.service.loantypegroup.step.CreateLoanTypeGroupStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class CreateLoanTypeGroupCommandHandler
        implements WorkflowCommandHandler<CreateLoanTypeGroupCommand, CreateLoanTypeGroupData> {

    @Override
    public Workflow<CreateLoanTypeGroupData> definition() {
        return Workflow.singleWrite("create-loan-type-group", writePublishing(step));
    }

    @Override
    public Result<CreateLoanTypeGroupData> seed(CreateLoanTypeGroupCommand command) {
        return Result.success(new CreateLoanTypeGroupData(command));
    }

    private final CreateLoanTypeGroupStep step;
}
