package ir.dotin.loan.trade.core.domain.disbursement.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.trade.core.domain.disbursement.vo.TradeDisbursementRecordId;

import static java.util.Objects.requireNonNull;

public record TradeDisbursementFailedEvent(
        UUID eventId, TradeDisbursementRecordId aggregateId, FailureReason payload, Instant createdAt)
        implements TradeDisbursementEvent<TradeDisbursementFailedEvent, FailureReason> {

    public static final String FAILED = "FAILED";

    public TradeDisbursementFailedEvent {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null");
        requireNonNull(payload, "failureReason cannot be null");
        requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + FAILED;
    }
}
