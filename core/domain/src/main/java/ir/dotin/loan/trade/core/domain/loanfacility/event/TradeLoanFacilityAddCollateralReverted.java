package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityAddCollateralReverted(
        UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityAddCollateralReverted> {

    public TradeLoanFacilityAddCollateralReverted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityAddCollateralReverted of(LoanFacilityId id, Clock clock) {
        return new TradeLoanFacilityAddCollateralReverted(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.ADD_COLLATERAL_REVERTED.getFullType(),
                clock.instant());
    }
}
