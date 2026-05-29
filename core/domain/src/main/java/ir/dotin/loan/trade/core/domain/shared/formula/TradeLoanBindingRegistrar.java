package ir.dotin.loan.trade.core.domain.shared.formula;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.formula.api.binding.FieldBinding;
import ir.dotin.platform.formula.api.spi.BindingRegistrar;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainComponent;

/**
 * SPI implementation to register Trade Loan parameters with the formula engine. Replaces the legacy static
 * TradeLoanBindingRegistry.
 */
@DomainComponent
public class TradeLoanBindingRegistrar implements BindingRegistrar {

    private final Map<String, FieldBinding<TradeLoanParameterProvider, ?>> bindings;
    private final ProviderInfo providerInfo;

    public TradeLoanBindingRegistrar() {
        Map<String, FieldBinding<TradeLoanParameterProvider, ?>> map = new HashMap<>();

        register(map, TradeLoanBindings.TRADE_APPROVED_AMOUNT);
        register(map, TradeLoanBindings.TRADE_REQUESTED_AMOUNT);
        register(map, TradeLoanBindings.TRADE_DURATION_MONTHS);
        register(map, TradeLoanBindings.TRADE_INSTALLMENT_COUNT);
        register(map, TradeLoanBindings.TRADE_GRACE_PERIOD);
        register(map, TradeLoanBindings.TRADE_TOTAL_DISBURSED);

        this.bindings = Map.copyOf(map);

        this.providerInfo = ProviderInfo.builder(TradeLoanParameterProvider.class)
                .name("TradeLoan")
                .displayName("Trade Loan Facility")
                .description("Parameters specific to Trade Loan facilities")
                .bindings(this.bindings.keySet())
                .build();
    }

    private void register(
            Map<String, FieldBinding<TradeLoanParameterProvider, ?>> map,
            FieldBinding<TradeLoanParameterProvider, ?> binding) {
        map.put(binding.fieldName(), binding);
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return providerInfo;
    }

    @Override
    public @NonNull FieldBinding<?, ?> resolveBinding(@NonNull String variableName) {
        FieldBinding<TradeLoanParameterProvider, ?> binding = bindings.get(variableName);
        if (binding == null) {
            throw new java.util.NoSuchElementException("No binding registered for variable '" + variableName
                    + "' in TradeLoanBindingRegistrar." + " Call supports() before resolveBinding().");
        }
        return binding;
    }
}
