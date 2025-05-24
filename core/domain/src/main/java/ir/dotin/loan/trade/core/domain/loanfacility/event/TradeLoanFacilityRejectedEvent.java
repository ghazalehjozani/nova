package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanApplicationId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;

import static java.util.Objects.requireNonNull;

public record TradeLoanFacilityRejectedEvent(
        UUID eventId, TradeLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<TradeLoanFacilityRejectedEvent, TradeLoanFacilityRejectedEvent.Payload> {

    public record Payload(TradeLoanApplicationId applicationId) {
        public Payload {
            requireNonNull(applicationId);
        }
    }

    public TradeLoanFacilityRejectedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityRejectedEvent of(TradeLoanFacilityId id, TradeLoanApplicationId appId, Clock clock) {
        return new TradeLoanFacilityRejectedEvent(UUID.randomUUID(), id, new Payload(appId), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "REJECTED";
    }
}
