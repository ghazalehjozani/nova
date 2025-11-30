package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.projection;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Projection for
 * {@link ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity}
 */
public interface TradeLoanArrangementIdProjection {
    @Nullable
    UUID getId();
}
