package ir.dotin.loan.trade.core.application.service.formula.handler;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.service.cqrs.query.EvaluateFormulaQuery;
import ir.dotin.platform.formula.service.cqrs.query.EvaluateFormulaResult;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.service.shared.formula.FormulaFailureTranslator;
import ir.dotin.loan.trade.core.application.service.shared.formula.TradeLoanFormulaEvaluationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluateFormulaQueryHandler implements QueryHandler<EvaluateFormulaQuery, EvaluateFormulaResult> {

    private final TradeLoanFormulaEvaluationService evaluationService;

    @Override
    public EvaluateFormulaResult handle(EvaluateFormulaQuery query) {
        try {
            BigDecimal value = evaluationService.evaluate(query.code(), query.providerRefs(), query.overrides());
            return new EvaluateFormulaResult(value, query.code(), false);
        } catch (RuntimeException exception) {
            throw FormulaFailureTranslator.toException(query.code(), exception);
        }
    }
}
