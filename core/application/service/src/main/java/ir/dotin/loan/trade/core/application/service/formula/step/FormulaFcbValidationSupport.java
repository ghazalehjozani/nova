package ir.dotin.loan.trade.core.application.service.formula.step;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.trade.core.application.ports.outbound.client.formula.ValidateFormulaInFcbPort;
import ir.dotin.loan.trade.core.application.service.configuration.FormulaCorridorProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FormulaFcbValidationSupport {

    private final ValidateFormulaInFcbPort validateFormulaInFcbPort;
    private final FormulaCorridorProperties properties;

    public StepResult<Void> validate(String code) {
        if (!properties.isValidateInFcbEnabled()) {
            return StepResult.success(null);
        }
        Result<Unit> result = validateFormulaInFcbPort.validateFormulaInFcb(code);
        if (result.isFailure()) {
            return StepResult.failure(result.err().orElseThrow());
        }
        return StepResult.success(null);
    }
}
