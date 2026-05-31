package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.projection;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;

/**
 * Narrow read projection for facility-state reconciliation: just the id, current state, and last-modified instant used
 * as the keyset paging cursor. Avoids loading the full facility graph during a reconciliation sweep.
 */
public interface FacilityReconStateProjection {

    @Nullable
    UUID getId();

    @Nullable
    FacilityStatus getCurrentState();

    @Nullable
    LocalDateTime getModifiedAt();
}
