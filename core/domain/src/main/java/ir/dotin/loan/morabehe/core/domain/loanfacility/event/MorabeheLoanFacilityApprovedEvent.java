package ir.dotin.loan.morabehe.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheSanctionedLoanId;

import static java.util.Objects.requireNonNull;

public record MorabeheLoanFacilityApprovedEvent(
        UUID eventId, MorabeheLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanFacilityEvent<
                MorabeheLoanFacilityApprovedEvent, MorabeheLoanFacilityApprovedEvent.Payload> {

    public record Payload(
            MorabeheSanctionedLoanId sanctionedLoanId, SanctionSerial sanctionSerial) { // Added sanctionedLoanId
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(sanctionSerial);
        }
    }

    public MorabeheLoanFacilityApprovedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static MorabeheLoanFacilityApprovedEvent of(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanId, SanctionSerial sanctionSerial, Clock clock) {
        return new MorabeheLoanFacilityApprovedEvent(
                UUID.randomUUID(), id, new Payload(sanId, sanctionSerial), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "APPROVED";
    }
}
