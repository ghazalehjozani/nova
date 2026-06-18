package ir.dotin.loan.trade.core.application.service.formula.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.service.cqrs.command.DeleteFormulaCommand;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.service.formula.step.DeleteFormulaWriteStep;
import ir.dotin.loan.trade.core.application.service.formula.workflow.DeleteFormulaData;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.write;

@Service
@RequiredArgsConstructor
public final class DeleteFormulaCommandHandler
        implements WorkflowCommandHandler<DeleteFormulaCommand, DeleteFormulaData> {

    @Override
    public Workflow<DeleteFormulaData> definition() {
        return Workflow.singleWrite("delete-formula", write(deleteFormulaWriteStep));
    }

    @Override
    public Result<DeleteFormulaData> seed(DeleteFormulaCommand command) {
        return Result.success(new DeleteFormulaData(command));
    }

    private final DeleteFormulaWriteStep deleteFormulaWriteStep;
}
