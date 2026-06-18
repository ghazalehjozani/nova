package ir.dotin.loan.trade.core.application.service.formula.step;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.RemoteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.trade.core.application.service.formula.workflow.CreateFormulaData;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidateCreateFormulaInFcbStep implements RemoteActivity<CreateFormulaData> {

    private final FormulaFcbValidationSupport validationSupport;

    @Override
    public StepResult<Void> execute(WorkflowContext<CreateFormulaData> ctx) {
        return validationSupport.validate(ctx.data().command().code());
    }
}
