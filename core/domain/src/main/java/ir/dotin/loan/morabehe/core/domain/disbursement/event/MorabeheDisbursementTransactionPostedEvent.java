package ir.dotin.loan.morabehe.core.domain.disbursement.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.morabehe.core.domain.disbursement.vo.MorabeheDisbursementRecordId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;

public record MorabeheDisbursementTransactionPostedEvent(
        UUID eventId, MorabeheDisbursementRecordId aggregateId, Instant createdAt, Payload payload)
        implements MorabeheDisbursementEvent<
                MorabeheDisbursementTransactionPostedEvent, MorabeheDisbursementTransactionPostedEvent.Payload> {

    public static final String PENDING = "PENDING";

    public MorabeheDisbursementTransactionPostedEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + PENDING;
    }

    public record Payload(MorabeheLoanFacilityId loanFacilityId) {
        public Payload {
            Objects.requireNonNull(loanFacilityId, "loanFacilityId cannot be null");
        }
    }

    public static MorabeheDisbursementTransactionPostedEvent create(
            UUID eventId,
            MorabeheDisbursementRecordId aggregateId,
            Instant createdAt,
            MorabeheLoanFacilityId loanFacilityId) {
        return new MorabeheDisbursementTransactionPostedEvent(
                eventId,
                aggregateId,
                createdAt,
                new MorabeheDisbursementTransactionPostedEvent.Payload(loanFacilityId));
    }
}
