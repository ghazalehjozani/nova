package ir.dotin.loan.trade.core.application.service.interaction;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaEvaluationContext;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaEvaluationResult;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaEvaluator;
import ir.dotin.loan.trade.core.application.ports.outbound.formula.FormulaEvaluatorPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FormulaEvaluatorService implements FormulaEvaluator {

    private final FormulaEvaluatorPort formulaEvaluatorPort;

    @Override
    public Result<FormulaEvaluationResult> evaluate(FormulaEvaluationContext context) {
        return formulaEvaluatorPort.evaluate(context);
    }
}
