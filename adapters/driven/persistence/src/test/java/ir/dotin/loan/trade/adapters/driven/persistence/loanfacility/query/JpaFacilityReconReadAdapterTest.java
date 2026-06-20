package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.projection.FacilityGuarantorProjection;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.projection.FacilityReconStateProjection;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconRow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Perf gate for the guarantor-drift feature (LN-59442 M1): the recon read adapter must issue the extra
 * {@code findGuarantorsById} query ONLY when the caller asks for guarantors ({@code includeGuarantors=true}), so a
 * recon probe pays nothing for the feature while it is dark.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class JpaFacilityReconReadAdapterTest {

    private static final UUID FACILITY = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private TradeLoanFacilityJpaRepository repository;

    @InjectMocks
    private JpaFacilityReconReadAdapter adapter;

    @Test
    void includeGuarantorsFalseSkipsTheGuarantorQuery() {
        FacilityReconStateProjection projection = reconProjection();
        when(repository.findReconStateById(FACILITY)).thenReturn(Optional.of(projection));

        Optional<FacilityReconRow> row = adapter.findById(FACILITY.toString(), false);

        assertThat(row).isPresent();
        assertThat(row.get().guarantors()).isEmpty();
        verify(repository, never()).findGuarantorsById(any());
    }

    @Test
    void defaultFindByIdSkipsTheGuarantorQuery() {
        FacilityReconStateProjection projection = reconProjection();
        when(repository.findReconStateById(FACILITY)).thenReturn(Optional.of(projection));

        Optional<FacilityReconRow> row = adapter.findById(FACILITY.toString());

        assertThat(row).isPresent();
        assertThat(row.get().guarantors()).isEmpty();
        verify(repository, never()).findGuarantorsById(any());
    }

    @Test
    void includeGuarantorsTrueProjectsGuarantors() {
        FacilityReconStateProjection projection = reconProjection();
        FacilityGuarantorProjection g1 = guarantorProjection("111", new BigDecimal("60"));
        FacilityGuarantorProjection g2 = guarantorProjection("222", null);
        when(repository.findReconStateById(FACILITY)).thenReturn(Optional.of(projection));
        when(repository.findGuarantorsById(FACILITY)).thenReturn(List.of(g1, g2));

        Optional<FacilityReconRow> row = adapter.findById(FACILITY.toString(), true);

        assertThat(row).isPresent();
        assertThat(row.get().guarantors()).hasSize(2);
        assertThat(row.get().guarantors())
                .anySatisfy(g -> assertThat(g.customerNumber()).isEqualTo("111"));
        verify(repository).findGuarantorsById(FACILITY);
    }

    @Test
    void malformedFacilityIdIsEmptyAndNeverQueries() {
        Optional<FacilityReconRow> row = adapter.findById("not-a-uuid", true);

        assertThat(row).isEmpty();
        verify(repository, never()).findReconStateById(any());
        verify(repository, never()).findGuarantorsById(any());
    }

    private static FacilityReconStateProjection reconProjection() {
        FacilityReconStateProjection p = mock(FacilityReconStateProjection.class);
        when(p.getId()).thenReturn(FACILITY);
        when(p.getCurrentState()).thenReturn(FacilityStatus.APPROVED);
        when(p.getModifiedAt()).thenReturn(LocalDateTime.parse("2026-06-20T10:00:00"));
        return p;
    }

    private static FacilityGuarantorProjection guarantorProjection(String customerNumber, BigDecimal percent) {
        FacilityGuarantorProjection p = mock(FacilityGuarantorProjection.class);
        when(p.getCustomerNumber()).thenReturn(customerNumber);
        when(p.getGuaranteePercentage()).thenReturn(percent);
        return p;
    }
}
