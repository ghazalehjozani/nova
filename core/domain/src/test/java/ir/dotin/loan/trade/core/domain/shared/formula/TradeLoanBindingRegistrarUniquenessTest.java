package ir.dotin.loan.trade.core.domain.shared.formula;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ir.dotin.platform.formula.api.spi.BindingRegistrar;

import static org.assertj.core.api.Assertions.assertThat;

class TradeLoanBindingRegistrarUniquenessTest {

    private final List<BindingRegistrar> registrars = List.of(
            new TradeLoanBindingRegistrar(),
            new TradeLoanArrangementBindingRegistrar(),
            new TradeLoanInstallmentBindingRegistrar());

    @Test
    @DisplayName("nova provider codes are globally distinct")
    void providerCodesAreDistinct() {
        List<String> codes =
                registrars.stream().map(r -> r.getProviderInfo().code()).toList();

        assertThat(codes).doesNotHaveDuplicates();
        assertThat(codes).containsExactlyInAnyOrder("LOAN_FACILITY", "LOAN_ARRANGEMENT", "INSTALLMENT");
    }

    @Test
    @DisplayName("nova binding-name sets are pairwise disjoint")
    void bindingNameSetsAreDisjoint() {
        Set<String> seen = new HashSet<>();
        int total = 0;

        for (BindingRegistrar registrar : registrars) {
            Set<String> names = registrar.getProviderInfo().bindingNames();
            total += names.size();
            seen.addAll(names);
        }

        assertThat(seen).hasSize(total);
    }
}
