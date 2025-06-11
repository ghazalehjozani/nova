package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanApplicationId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityCreatedEvent(
        UUID eventId, TradeLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<TradeLoanFacilityCreatedEvent, TradeLoanFacilityCreatedEvent.Payload> {

    public record Payload(TradeLoanApplicationId applicationId, Party customer) {
        public Payload {
            requireNonNull(applicationId);
            requireNonNull(customer);
        }
    }

    public TradeLoanFacilityCreatedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityCreatedEvent of(
            TradeLoanFacilityId id, TradeLoanApplicationId appId, Party customer, Clock clock) {
        return new TradeLoanFacilityCreatedEvent(randomUUID(), id, new Payload(appId, customer), clock.instant());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CREATED";
    }
}
