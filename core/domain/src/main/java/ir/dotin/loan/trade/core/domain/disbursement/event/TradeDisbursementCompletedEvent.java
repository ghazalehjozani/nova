package ir.dotin.loan.trade.core.domain.disbursement.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;

import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.trade.core.domain.disbursement.vo.TradeDisbursementRecordId;

import static java.util.Objects.requireNonNull;

public record TradeDisbursementCompletedEvent(
        UUID eventId,
        TradeDisbursementRecordId aggregateId,
        List<TransactionNumber> transactionNumbers,
        Instant createdAt)
        implements TradeDisbursementEvent<TradeDisbursementCompletedEvent, List<TransactionNumber>> {

    public static final String COMPLETED = "COMPLETED";

    public TradeDisbursementCompletedEvent {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null");
        requireNonNull(transactionNumbers, "transactionNumbers cannot be null");
        requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public List<TransactionNumber> payload() {
        return ImmutableList.of();
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + COMPLETED;
    }
}
