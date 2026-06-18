package ir.dotin.loan.trade.core.application.query.formula.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.core.binding.BindingDiscoveryService;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.formula.dto.DiscoveryStatsView;
import ir.dotin.loan.trade.core.application.query.formula.request.GetDiscoveryStatsQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetDiscoveryStatsQueryHandler implements QueryHandler<GetDiscoveryStatsQuery, DiscoveryStatsView> {

    private final BindingDiscoveryService bindingDiscoveryService;

    @Override
    public DiscoveryStatsView handle(GetDiscoveryStatsQuery query) {
        return new DiscoveryStatsView(
                bindingDiscoveryService.getProviderCount(), bindingDiscoveryService.getBindingCount());
    }
}
