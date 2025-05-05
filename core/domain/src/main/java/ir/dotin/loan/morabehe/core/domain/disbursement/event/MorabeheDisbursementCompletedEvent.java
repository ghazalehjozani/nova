package ir.dotin.loan.morabehe.core.domain.disbursement.event;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.TransactionNumber;
import ir.dotin.loan.morabehe.core.domain.disbursement.vo.MorabeheDisbursementRecordId;

public record MorabeheDisbursementCompletedEvent(
        UUID eventId,
        MorabeheDisbursementRecordId aggregateId,
        List<TransactionNumber> transactionNumbers,
        Instant createdAt)
        implements MorabeheDisbursementEvent<MorabeheDisbursementCompletedEvent, List<TransactionNumber>> {

    public static final String COMPLETED = "COMPLETED";

    public MorabeheDisbursementCompletedEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(transactionNumbers, "transactionNumbers cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public List<TransactionNumber> payload() {
        return List.of();
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + COMPLETED;
    }
}
