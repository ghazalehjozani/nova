package ir.dotin.loan.morabehe.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheSanctionedLoanId;

import static java.util.Objects.requireNonNull;

public record MorabeheLoanFacilityPendingDisbursementEvent(
        UUID eventId, MorabeheLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanFacilityEvent<
                MorabeheLoanFacilityPendingDisbursementEvent, MorabeheLoanFacilityPendingDisbursementEvent.Payload> {

    public record Payload(MorabeheSanctionedLoanId sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public MorabeheLoanFacilityPendingDisbursementEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static MorabeheLoanFacilityPendingDisbursementEvent of(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanId, Clock clock) {
        return new MorabeheLoanFacilityPendingDisbursementEvent(
                UUID.randomUUID(), id, new Payload(sanId), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "PENDING_DISBURSEMENT";
    }
}
