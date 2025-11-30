package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.projection;


import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Projection for {@link ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity}
 */
public interface TradeLoanArrangementIdProjection {
    @Nullable
    UUID getId();
}