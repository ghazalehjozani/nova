package ir.dotin.loan.trade.core.application.ports.outbound.formula;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaEvaluationContext;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaEvaluationResult;

public interface FormulaEvaluatorPort {
    Result<FormulaEvaluationResult> evaluate(FormulaEvaluationContext context);
}
