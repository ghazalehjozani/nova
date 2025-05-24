package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

import static java.util.Objects.requireNonNull;

public record TradeLoanFacilityCancelledEvent(
        UUID eventId, TradeLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<TradeLoanFacilityCancelledEvent, TradeLoanFacilityCancelledEvent.Payload> {

    public record Payload(Optional<TradeSanctionedLoanId> sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public TradeLoanFacilityCancelledEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityCancelledEvent of(TradeLoanFacilityId id, Clock clock) {
        return new TradeLoanFacilityCancelledEvent(
                UUID.randomUUID(), id, new Payload(Optional.empty()), Instant.now(clock));
    }

    public static TradeLoanFacilityCancelledEvent of(TradeLoanFacilityId id, TradeSanctionedLoanId sanId, Clock clock) {
        return new TradeLoanFacilityCancelledEvent(
                UUID.randomUUID(), id, new Payload(Optional.of(sanId)), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CANCELLED";
    }
}
