package ir.dotin.loan.trade.core.application.service.formula.step;

import org.springframework.stereotype.Component;

import ir.dotin.platform.formula.service.FormulaDefinitionService;
import ir.dotin.platform.formula.service.cqrs.command.DeleteFormulaCommand;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.WriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.trade.core.application.service.formula.workflow.DeleteFormulaData;
import ir.dotin.loan.trade.core.application.service.shared.formula.FormulaFailureTranslator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteFormulaWriteStep implements WriteActivity<DeleteFormulaData> {

    private final FormulaDefinitionService formulaDefinitionService;

    @Override
    public StepResult<Void> execute(WorkflowContext<DeleteFormulaData> ctx) {
        DeleteFormulaCommand command = ctx.data().command();
        try {
            formulaDefinitionService.delete(command);
            return StepResult.success(null);
        } catch (RuntimeException exception) {
            return StepResult.failure(FormulaFailureTranslator.toFailureCause(command.code(), exception));
        }
    }
}
