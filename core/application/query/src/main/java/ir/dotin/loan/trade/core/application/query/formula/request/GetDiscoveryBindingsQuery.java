package ir.dotin.loan.trade.core.application.query.formula.request;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.loan.trade.core.application.query.formula.dto.BindingNamesView;

public record GetDiscoveryBindingsQuery() implements Query<BindingNamesView> {

    @Override
    public Class<BindingNamesView> getResultType() {
        return BindingNamesView.class;
    }
}
