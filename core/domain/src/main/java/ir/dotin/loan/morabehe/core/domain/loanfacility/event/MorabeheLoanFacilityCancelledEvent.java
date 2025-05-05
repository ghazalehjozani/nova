package ir.dotin.loan.morabehe.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheSanctionedLoanId;

import static java.util.Objects.requireNonNull;

public record MorabeheLoanFacilityCancelledEvent(
        UUID eventId, MorabeheLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanFacilityEvent<
                MorabeheLoanFacilityCancelledEvent, MorabeheLoanFacilityCancelledEvent.Payload> {

    public record Payload(Optional<MorabeheSanctionedLoanId> sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public MorabeheLoanFacilityCancelledEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static MorabeheLoanFacilityCancelledEvent of(MorabeheLoanFacilityId id, Clock clock) {
        return new MorabeheLoanFacilityCancelledEvent(
                UUID.randomUUID(), id, new Payload(Optional.empty()), Instant.now(clock));
    }

    public static MorabeheLoanFacilityCancelledEvent of(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanId, Clock clock) {
        return new MorabeheLoanFacilityCancelledEvent(
                UUID.randomUUID(), id, new Payload(Optional.of(sanId)), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CANCELLED";
    }
}
