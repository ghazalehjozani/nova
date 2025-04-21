package ir.dotin.loan.morabehe.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheSanctionedLoanId;

import static java.util.Objects.requireNonNull;

public record MorabeheLoanFacilityClosedDefaultedEvent(
        UUID eventId, MorabeheLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanFacilityEvent<
                MorabeheLoanFacilityClosedDefaultedEvent, MorabeheLoanFacilityClosedDefaultedEvent.Payload> {

    public record Payload(MorabeheSanctionedLoanId sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public MorabeheLoanFacilityClosedDefaultedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static MorabeheLoanFacilityClosedDefaultedEvent of(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanId, Clock clock) {
        return new MorabeheLoanFacilityClosedDefaultedEvent(
                UUID.randomUUID(), id, new Payload(sanId), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CLOSED_DEFAULTED";
    }
}
