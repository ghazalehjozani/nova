package ir.dotin.loan.trade.core.application.query.formula.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.core.binding.BindingDiscoveryService;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.formula.dto.BindingNamesView;
import ir.dotin.loan.trade.core.application.query.formula.request.GetDiscoveryBindingsQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetDiscoveryBindingsQueryHandler implements QueryHandler<GetDiscoveryBindingsQuery, BindingNamesView> {

    private final BindingDiscoveryService bindingDiscoveryService;

    @Override
    public BindingNamesView handle(GetDiscoveryBindingsQuery query) {
        return new BindingNamesView(bindingDiscoveryService.getAllBindingNames());
    }
}
