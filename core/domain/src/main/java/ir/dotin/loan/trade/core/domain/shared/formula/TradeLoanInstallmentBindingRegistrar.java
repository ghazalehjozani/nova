package ir.dotin.loan.trade.core.domain.shared.formula;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.formula.api.binding.FieldBinding;
import ir.dotin.platform.formula.api.spi.BindingRegistrar;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainComponent;

@DomainComponent
public class TradeLoanInstallmentBindingRegistrar implements BindingRegistrar {

    private final Map<String, FieldBinding<TradeLoanInstallmentParameterProvider, ?>> bindings;
    private final ProviderInfo providerInfo;

    public TradeLoanInstallmentBindingRegistrar() {
        Map<String, FieldBinding<TradeLoanInstallmentParameterProvider, ?>> map = new HashMap<>();

        register(map, TradeLoanInstallmentBindings.TRADE_TOTAL_INTEREST);
        register(map, TradeLoanInstallmentBindings.TRADE_OUTSTANDING_PRINCIPAL);
        register(map, TradeLoanInstallmentBindings.TRADE_PAID_PRINCIPAL);
        register(map, TradeLoanInstallmentBindings.TRADE_TOTAL_LOAN_AMOUNT);
        register(map, TradeLoanInstallmentBindings.TRADE_INSTALLMENT_SCHEDULE_COUNT);

        this.bindings = Map.copyOf(map);

        this.providerInfo = ProviderInfo.builder(TradeLoanInstallmentParameterProvider.class)
                .code("INSTALLMENT")
                .name("TradeLoanInstallment")
                .displayName("Trade Loan Installment Schedule")
                .description("Aggregate amount parameters of a Trade Loan installment schedule")
                .bindings(this.bindings.keySet())
                .build();
    }

    private void register(
            Map<String, FieldBinding<TradeLoanInstallmentParameterProvider, ?>> map,
            FieldBinding<TradeLoanInstallmentParameterProvider, ?> binding) {
        map.put(binding.fieldName(), binding);
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return providerInfo;
    }

    @Override
    public @NonNull FieldBinding<?, ?> resolveBinding(@NonNull String variableName) {
        FieldBinding<TradeLoanInstallmentParameterProvider, ?> binding = bindings.get(variableName);
        if (binding == null) {
            throw new java.util.NoSuchElementException("No binding registered for variable '" + variableName
                    + "' in TradeLoanInstallmentBindingRegistrar." + " Call supports() before resolveBinding().");
        }
        return binding;
    }
}
