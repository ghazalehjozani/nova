package ir.dotin.loan.trade.core.application.query.formula.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.service.query.FormulaQueryService;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.formula.dto.RegisteredBindingsView;
import ir.dotin.loan.trade.core.application.query.formula.request.GetRegisteredBindingsQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetRegisteredBindingsQueryHandler
        implements QueryHandler<GetRegisteredBindingsQuery, RegisteredBindingsView> {

    private final FormulaQueryService formulaQueryService;

    @Override
    public RegisteredBindingsView handle(GetRegisteredBindingsQuery query) {
        if (query.isAllProviders()) {
            return new RegisteredBindingsView(formulaQueryService.getAllRegisteredBindings());
        }
        return new RegisteredBindingsView(formulaQueryService.getRegisteredBindings(query.providerCode()));
    }
}
