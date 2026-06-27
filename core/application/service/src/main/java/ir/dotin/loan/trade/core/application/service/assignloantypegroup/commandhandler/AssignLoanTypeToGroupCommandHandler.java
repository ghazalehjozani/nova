package ir.dotin.loan.trade.core.application.service.assignloantypegroup.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AssignLoanTypeToGroupCommand;
import ir.dotin.loan.trade.core.application.service.assignloantypegroup.data.AssignLoanTypeToGroupData;
import ir.dotin.loan.trade.core.application.service.assignloantypegroup.step.AssignLoanTypeToGroupStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class AssignLoanTypeToGroupCommandHandler
        implements WorkflowCommandHandler<AssignLoanTypeToGroupCommand, AssignLoanTypeToGroupData> {

    @Override
    public Workflow<AssignLoanTypeToGroupData> definition() {
        return Workflow.singleWrite("assign-loan-type-to-group", writePublishing(step));
    }

    @Override
    public Result<AssignLoanTypeToGroupData> seed(AssignLoanTypeToGroupCommand command) {
        return Result.success(new AssignLoanTypeToGroupData(command));
    }

    private final AssignLoanTypeToGroupStep step;
}
