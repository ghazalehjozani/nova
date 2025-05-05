package ir.dotin.loan.morabehe.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanApplicationId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;

import static java.util.Objects.requireNonNull;

public record MorabeheLoanFacilityRejectedEvent(
        UUID eventId, MorabeheLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanFacilityEvent<
                MorabeheLoanFacilityRejectedEvent, MorabeheLoanFacilityRejectedEvent.Payload> {

    public record Payload(MorabeheLoanApplicationId applicationId) {
        public Payload {
            requireNonNull(applicationId);
        }
    }

    public MorabeheLoanFacilityRejectedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static MorabeheLoanFacilityRejectedEvent of(
            MorabeheLoanFacilityId id, MorabeheLoanApplicationId appId, Clock clock) {
        return new MorabeheLoanFacilityRejectedEvent(UUID.randomUUID(), id, new Payload(appId), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "REJECTED";
    }
}
