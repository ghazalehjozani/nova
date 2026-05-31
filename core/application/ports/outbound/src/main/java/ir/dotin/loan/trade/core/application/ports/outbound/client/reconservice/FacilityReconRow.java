package ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;

/**
 * Narrow read-model row for facility-state reconciliation: just the facility id, its current Nova
 * {@link FacilityStatus}, and the last-modified instant (epoch millis) used as the paging cursor. Deliberately minimal
 * — the reconciliation probe never needs the full aggregate.
 *
 * @param facilityId the facility aggregate id as a string (the reconciliation opaque key).
 * @param status the current Nova facility status.
 * @param modifiedAtEpochMs the row's last-modified instant in epoch milliseconds (0 when unknown).
 */
public record FacilityReconRow(String facilityId, FacilityStatus status, long modifiedAtEpochMs) {}
