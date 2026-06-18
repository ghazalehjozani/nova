package ir.dotin.loan.trade.core.domain.shared.formula;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.formula.api.binding.FieldBinding;
import ir.dotin.platform.formula.api.spi.BindingRegistrar;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainComponent;

@DomainComponent
public class TradeLoanArrangementBindingRegistrar implements BindingRegistrar {

    private final Map<String, FieldBinding<TradeLoanArrangementParameterProvider, ?>> bindings;
    private final ProviderInfo providerInfo;

    public TradeLoanArrangementBindingRegistrar() {
        Map<String, FieldBinding<TradeLoanArrangementParameterProvider, ?>> map = new HashMap<>();

        register(map, TradeLoanArrangementBindings.TRADE_INTEREST_RATE);
        register(map, TradeLoanArrangementBindings.TRADE_PENALTY_RATE);

        this.bindings = Map.copyOf(map);

        this.providerInfo = ProviderInfo.builder(TradeLoanArrangementParameterProvider.class)
                .code("LOAN_ARRANGEMENT")
                .name("TradeLoanArrangement")
                .displayName("Trade Loan Arrangement")
                .description("Rate parameters specific to Trade Loan arrangements")
                .bindings(this.bindings.keySet())
                .build();
    }

    private void register(
            Map<String, FieldBinding<TradeLoanArrangementParameterProvider, ?>> map,
            FieldBinding<TradeLoanArrangementParameterProvider, ?> binding) {
        map.put(binding.fieldName(), binding);
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return providerInfo;
    }

    @Override
    public @NonNull FieldBinding<?, ?> resolveBinding(@NonNull String variableName) {
        FieldBinding<TradeLoanArrangementParameterProvider, ?> binding = bindings.get(variableName);
        if (binding == null) {
            throw new java.util.NoSuchElementException("No binding registered for variable '" + variableName
                    + "' in TradeLoanArrangementBindingRegistrar." + " Call supports() before resolveBinding().");
        }
        return binding;
    }
}
