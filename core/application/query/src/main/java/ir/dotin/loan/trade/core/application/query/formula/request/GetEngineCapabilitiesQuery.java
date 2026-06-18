package ir.dotin.loan.trade.core.application.query.formula.request;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.loan.trade.core.application.query.formula.dto.EngineCapabilitiesView;

public record GetEngineCapabilitiesQuery() implements Query<EngineCapabilitiesView> {

    @Override
    public Class<EngineCapabilitiesView> getResultType() {
        return EngineCapabilitiesView.class;
    }
}
