package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityCreatedEvent(
        UUID eventId, LoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<TradeLoanFacilityCreatedEvent, TradeLoanFacilityCreatedEvent.Payload> {

    public record Payload(LoanApplicationId applicationId, Party customer) {
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
            LoanFacilityId id, LoanApplicationId appId, Party customer, Clock clock) {
        return new TradeLoanFacilityCreatedEvent(randomUUID(), id, new Payload(appId, customer), clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "CREATED";
    }
}
