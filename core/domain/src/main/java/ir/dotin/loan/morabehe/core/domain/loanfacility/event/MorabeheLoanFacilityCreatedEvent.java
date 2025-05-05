package ir.dotin.loan.morabehe.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanApplicationId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;

import static java.util.Objects.requireNonNull;

public record MorabeheLoanFacilityCreatedEvent(
        UUID eventId, MorabeheLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanFacilityEvent<
                MorabeheLoanFacilityCreatedEvent, MorabeheLoanFacilityCreatedEvent.Payload> {

    public record Payload(MorabeheLoanApplicationId applicationId, Party customer) {
        public Payload {
            requireNonNull(applicationId);
            requireNonNull(customer);
        }
    }

    public MorabeheLoanFacilityCreatedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static MorabeheLoanFacilityCreatedEvent of(
            MorabeheLoanFacilityId id, MorabeheLoanApplicationId appId, Party customer, Clock clock) {
        return new MorabeheLoanFacilityCreatedEvent(
                UUID.randomUUID(), id, new Payload(appId, customer), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CREATED";
    }
}
