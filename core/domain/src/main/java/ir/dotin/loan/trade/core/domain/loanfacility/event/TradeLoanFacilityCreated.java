package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityCreated(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityCreated, TradeLoanFacilityCreated.Payload> {

    public record Payload(String applicationNumber) {
        public Payload {
            requireNonNull(applicationNumber);
        }
    }

    public TradeLoanFacilityCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityCreated of(LoanFacilityId id, String applicationNumber, Clock clock) {
        return new TradeLoanFacilityCreated(
                randomUUID(),
                id.value(),
                TradeLoanFacilityCreated.class.getSimpleName(),
                TradeLoanFacilityEventType.CREATED.getFullType(),
                new Payload(applicationNumber),
                clock.instant());
    }
}
