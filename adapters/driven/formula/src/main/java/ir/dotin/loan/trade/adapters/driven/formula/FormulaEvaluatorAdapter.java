package ir.dotin.loan.trade.adapters.driven.formula;

import java.util.Map;
import java.util.stream.Collectors;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaEvaluationContext;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaEvaluationResult;
import ir.dotin.loan.trade.core.application.ports.outbound.formula.FormulaEvaluatorPort;

import lombok.SneakyThrows;

@Component
public class FormulaEvaluatorAdapter implements FormulaEvaluatorPort {

    @SneakyThrows // TODO: handle with i18n
    @Override
    public Result<FormulaEvaluationResult> evaluate(FormulaEvaluationContext context) {
        Expression expression = new Expression(context.formula());

        Map<String, Object> stringParameterValues = context.parameterValues().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()), entry -> normalizeForEvaluation(entry.getValue())));

        EvaluationValue result = expression.withValues(stringParameterValues).evaluate();
        return Result.success(new FormulaEvaluationResult(result.getNumberValue()));
    }

    private Object normalizeForEvaluation(Object value) {
        return switch (value) {
            case Money money -> money.value();
            case null -> throw new IllegalArgumentException("Parameter value cannot be null");
            default -> value;
        };
    }
}
