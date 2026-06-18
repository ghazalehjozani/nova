package ir.dotin.loan.trade.core.application.query.formula.request;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.loan.trade.core.application.query.formula.dto.ProviderTypesView;

public record GetRegisteredProviderTypesQuery() implements Query<ProviderTypesView> {

    @Override
    public Class<ProviderTypesView> getResultType() {
        return ProviderTypesView.class;
    }
}
