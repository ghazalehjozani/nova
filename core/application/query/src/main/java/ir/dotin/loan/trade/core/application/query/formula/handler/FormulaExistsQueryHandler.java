package ir.dotin.loan.trade.core.application.query.formula.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.service.query.FormulaQueryService;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.formula.dto.FormulaExistsView;
import ir.dotin.loan.trade.core.application.query.formula.request.FormulaExistsQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FormulaExistsQueryHandler implements QueryHandler<FormulaExistsQuery, FormulaExistsView> {

    private final FormulaQueryService formulaQueryService;

    @Override
    public FormulaExistsView handle(FormulaExistsQuery query) {
        return new FormulaExistsView(query.code(), formulaQueryService.exists(query.code()));
    }
}
