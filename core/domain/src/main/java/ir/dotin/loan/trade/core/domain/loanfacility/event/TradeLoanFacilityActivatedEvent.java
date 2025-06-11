package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityActivatedEvent(
        UUID eventId, TradeLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<TradeLoanFacilityActivatedEvent, TradeLoanFacilityActivatedEvent.Payload> {

    public record Payload(TradeSanctionedLoanId sanctionedLoanId) { // Added sanctionedLoanId
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public TradeLoanFacilityActivatedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityActivatedEvent of(TradeLoanFacilityId id, TradeSanctionedLoanId sanId, Clock clock) {
        return new TradeLoanFacilityActivatedEvent(randomUUID(), id, new Payload(sanId), clock.instant());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "ACTIVATED";
    } // Renamed type
}
