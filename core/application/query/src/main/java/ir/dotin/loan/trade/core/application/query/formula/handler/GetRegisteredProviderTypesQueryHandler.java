package ir.dotin.loan.trade.core.application.query.formula.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.service.query.FormulaQueryService;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.formula.dto.ProviderTypesView;
import ir.dotin.loan.trade.core.application.query.formula.request.GetRegisteredProviderTypesQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetRegisteredProviderTypesQueryHandler
        implements QueryHandler<GetRegisteredProviderTypesQuery, ProviderTypesView> {

    private final FormulaQueryService formulaQueryService;

    @Override
    public ProviderTypesView handle(GetRegisteredProviderTypesQuery query) {
        return new ProviderTypesView(formulaQueryService.getRegisteredProviderTypes());
    }
}
