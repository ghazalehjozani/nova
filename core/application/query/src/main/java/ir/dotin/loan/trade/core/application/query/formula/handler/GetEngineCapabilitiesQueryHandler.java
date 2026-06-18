package ir.dotin.loan.trade.core.application.query.formula.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.service.query.FormulaQueryService;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.formula.dto.EngineCapabilitiesView;
import ir.dotin.loan.trade.core.application.query.formula.request.GetEngineCapabilitiesQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetEngineCapabilitiesQueryHandler
        implements QueryHandler<GetEngineCapabilitiesQuery, EngineCapabilitiesView> {

    private final FormulaQueryService formulaQueryService;

    @Override
    public EngineCapabilitiesView handle(GetEngineCapabilitiesQuery query) {
        return new EngineCapabilitiesView(formulaQueryService.getEngineCapabilities());
    }
}
