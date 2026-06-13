package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.projection;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;

/**
 * Narrow read projection for facility-state reconciliation: the id, current state, last-modified instant used as the
 * keyset paging cursor, and the four application-number component columns used to format the operator-visible
 * application number. Avoids loading the full facility graph during a reconciliation sweep.
 */
public interface FacilityReconStateProjection {

    @Nullable
    UUID getId();

    @Nullable
    FacilityStatus getCurrentState();

    @Nullable
    LocalDateTime getModifiedAt();

    @Nullable
    String getApplicationBranchCode();

    @Nullable
    String getApplicationLoanTypeCode();

    @Nullable
    String getApplicationCustomerNumber();

    @Nullable
    String getApplicationDerivedValue();
}
