package ir.dotin.loan.trade.core.application.query.formula.request;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.loan.trade.core.application.query.formula.dto.RegisteredBindingsView;

public record GetRegisteredBindingsQuery(@Nullable String providerCode) implements Query<RegisteredBindingsView> {

    @Override
    public Class<RegisteredBindingsView> getResultType() {
        return RegisteredBindingsView.class;
    }

    public boolean isAllProviders() {
        return providerCode == null || providerCode.isBlank();
    }
}
