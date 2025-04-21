package ir.dotin.loan.morabehe.core.domain.disbursement.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.morabehe.core.domain.disbursement.vo.MorabeheDisbursementRecordId;

public record MorabeheDisbursementPendingConfirmationEvent(
        UUID eventId, MorabeheDisbursementRecordId aggregateId, Instant createdAt)
        implements MorabeheDisbursementEvent<MorabeheDisbursementPendingConfirmationEvent, Void> {

    public static final String PENDING = "PENDING";

    public MorabeheDisbursementPendingConfirmationEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + PENDING;
    }

    @Override
    public Void payload() {
        return null;
    }
}
