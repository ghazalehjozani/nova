package ir.dotin.loan.morabehe.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheSanctionedLoanId;

import static java.util.Objects.requireNonNull;

public record MorabeheLoanFacilityActivatedEvent(
        UUID eventId, MorabeheLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanFacilityEvent<
                MorabeheLoanFacilityActivatedEvent, MorabeheLoanFacilityActivatedEvent.Payload> {

    public record Payload(MorabeheSanctionedLoanId sanctionedLoanId) { // Added sanctionedLoanId
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public MorabeheLoanFacilityActivatedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static MorabeheLoanFacilityActivatedEvent of(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanId, Clock clock) {
        return new MorabeheLoanFacilityActivatedEvent(UUID.randomUUID(), id, new Payload(sanId), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "ACTIVATED";
    } // Renamed type
}
