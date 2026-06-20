package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.projection;

import java.math.BigDecimal;

import org.jspecify.annotations.Nullable;

/**
 * Narrow read projection for a single guarantor party of a facility, used by facility-state reconciliation to compare
 * Nova's current guarantor set against FCB's. Reads only the two columns the drift detector needs — the guarantor's
 * customer number and guarantee percentage — straight from the persisted guarantor parties, without loading the full
 * facility graph or touching the command aggregate.
 */
public interface FacilityGuarantorProjection {

    @Nullable
    String getCustomerNumber();

    @Nullable
    BigDecimal getGuaranteePercentage();
}
