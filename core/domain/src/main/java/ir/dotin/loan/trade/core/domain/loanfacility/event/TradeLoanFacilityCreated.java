package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityCreated(
        UUID eventId, UUID aggregateId, String eventType, String applicationNumber, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityCreated> {

    public TradeLoanFacilityCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(applicationNumber);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityCreated of(LoanFacilityId id, String applicationNumber, Clock clock) {
        return new TradeLoanFacilityCreated(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.CREATED.getFullType(),
                applicationNumber,
                clock.instant());
    }
}
