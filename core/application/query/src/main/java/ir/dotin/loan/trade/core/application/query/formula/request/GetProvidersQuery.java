package ir.dotin.loan.trade.core.application.query.formula.request;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.loan.trade.core.application.query.formula.dto.ProviderListView;

public record GetProvidersQuery() implements Query<ProviderListView> {

    @Override
    public Class<ProviderListView> getResultType() {
        return ProviderListView.class;
    }
}
