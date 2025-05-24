package ir.dotin.loan.trade.core.domain.disbursement.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.trade.core.domain.disbursement.vo.TradeDisbursementRecordId;

public record TradeDisbursementFailedEvent(
        UUID eventId, TradeDisbursementRecordId aggregateId, FailureReason payload, Instant createdAt)
        implements TradeDisbursementEvent<TradeDisbursementFailedEvent, FailureReason> {

    public static final String FAILED = "FAILED";

    public TradeDisbursementFailedEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(payload, "failureReason cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + FAILED;
    }
}
