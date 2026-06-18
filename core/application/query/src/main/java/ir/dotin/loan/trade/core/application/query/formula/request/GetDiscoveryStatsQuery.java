package ir.dotin.loan.trade.core.application.query.formula.request;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.loan.trade.core.application.query.formula.dto.DiscoveryStatsView;

public record GetDiscoveryStatsQuery() implements Query<DiscoveryStatsView> {

    @Override
    public Class<DiscoveryStatsView> getResultType() {
        return DiscoveryStatsView.class;
    }
}
