package ir.dotin.loan.trade.core.application.query.formula.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.core.binding.BindingDiscoveryService;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.formula.dto.ProviderListView;
import ir.dotin.loan.trade.core.application.query.formula.request.GetProvidersQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProvidersQueryHandler implements QueryHandler<GetProvidersQuery, ProviderListView> {

    private final BindingDiscoveryService bindingDiscoveryService;

    @Override
    public ProviderListView handle(GetProvidersQuery query) {
        return new ProviderListView(List.copyOf(bindingDiscoveryService.getAllProviders()));
    }
}
