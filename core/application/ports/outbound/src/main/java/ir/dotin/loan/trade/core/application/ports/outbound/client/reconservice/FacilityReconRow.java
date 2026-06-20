package ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice;

import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;

/**
 * Narrow read-model row for facility-state reconciliation: just the facility id, its current Nova
 * {@link FacilityStatus}, the last-modified instant (epoch millis) used as the paging cursor, the formatted application
 * number for operator visibility, and Nova's current guarantor set (for the guarantor-drift detector). Deliberately
 * minimal — the reconciliation probe never needs the full aggregate.
 *
 * @param facilityId the facility aggregate id as a string (the reconciliation opaque key).
 * @param status the current Nova facility status.
 * @param modifiedAtEpochMs the row's last-modified instant in epoch milliseconds (0 when unknown).
 * @param applicationNumber the formatted application number ({@code branch-loanType-customer-derived}), or {@code null}
 *     when any component is absent.
 * @param guarantors Nova's current guarantor set for the facility (customer number + guarantee percentage), read from
 *     the persisted guarantor parties; empty when the facility has no guarantors. Populated only by {@code findById}
 *     (the single-facility probe path); the paging path leaves it empty (a sweep does not compare guarantors).
 */
public record FacilityReconRow(
        String facilityId,
        FacilityStatus status,
        long modifiedAtEpochMs,
        @Nullable String applicationNumber,
        List<ReconGuarantor> guarantors) {

    public FacilityReconRow {
        guarantors = guarantors == null ? List.of() : List.copyOf(guarantors);
    }

    /** Convenience for the paging path that never inspects guarantors: defaults to an empty guarantor set. */
    public FacilityReconRow(
            String facilityId, FacilityStatus status, long modifiedAtEpochMs, @Nullable String applicationNumber) {
        this(facilityId, status, modifiedAtEpochMs, applicationNumber, List.of());
    }
}
