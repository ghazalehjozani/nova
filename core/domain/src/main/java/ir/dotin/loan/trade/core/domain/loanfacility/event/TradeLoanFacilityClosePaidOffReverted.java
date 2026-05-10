package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityClosePaidOffReverted(UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityClosePaidOffReverted> {

    public TradeLoanFacilityClosePaidOffReverted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityClosePaidOffReverted of(LoanFacilityId id, Clock clock) {
        return new TradeLoanFacilityClosePaidOffReverted(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.CLOSED_PAID_OFF_REVERTED.getFullType(),
                clock.instant());
    }
}
