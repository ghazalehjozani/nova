package ir.dotin.loan.morabehe.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheSanctionedLoanId;

import static java.util.Objects.requireNonNull;

public record MorabeheLoanFacilityDisbursementFailedEvent(
        UUID eventId, MorabeheLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanFacilityEvent<
                MorabeheLoanFacilityDisbursementFailedEvent, MorabeheLoanFacilityDisbursementFailedEvent.Payload> {

    public record Payload(MorabeheSanctionedLoanId sanctionedLoanId, FailureReason reason) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(reason);
        }
    }

    public MorabeheLoanFacilityDisbursementFailedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static MorabeheLoanFacilityDisbursementFailedEvent of(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanId, FailureReason reason, Clock clock) {
        return new MorabeheLoanFacilityDisbursementFailedEvent(
                UUID.randomUUID(), id, new Payload(sanId, reason), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "DISBURSEMENT_FAILED";
    }
}
