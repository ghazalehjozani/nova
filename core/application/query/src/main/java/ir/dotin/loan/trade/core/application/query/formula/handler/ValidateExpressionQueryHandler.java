package ir.dotin.loan.trade.core.application.query.formula.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.service.query.FormulaQueryService;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.formula.dto.ValidationView;
import ir.dotin.loan.trade.core.application.query.formula.request.ValidateExpressionQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateExpressionQueryHandler implements QueryHandler<ValidateExpressionQuery, ValidationView> {

    private final FormulaQueryService formulaQueryService;

    @Override
    public ValidationView handle(ValidateExpressionQuery query) {
        return new ValidationView(formulaQueryService.validate(query.expression()));
    }
}
