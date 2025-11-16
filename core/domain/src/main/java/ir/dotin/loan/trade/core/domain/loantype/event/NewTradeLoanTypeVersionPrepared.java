package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record NewTradeLoanTypeVersionPrepared(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        LoanTypeId newAggregateId,
        LoanTypeId previousAggregateId,
        Instant createdAt)
        implements TradeLoanTypeEvents<NewTradeLoanTypeVersionPrepared> {

    public NewTradeLoanTypeVersionPrepared {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(newAggregateId);
        requireNonNull(previousAggregateId);
        requireNonNull(createdAt);
        if (!aggregateId.equals(newAggregateId.value()))
            throw new IllegalArgumentException("aggregateId must match newAggregateId");
    }

    public static NewTradeLoanTypeVersionPrepared of(
            LoanTypeId newAggregateId, LoanTypeId previousAggregateId, Clock clock) {
        return new NewTradeLoanTypeVersionPrepared(
                randomUUID(),
                newAggregateId.value(),
                TradeLoanTypeEventType.VERSION_PREPARED.getFullType(),
                newAggregateId,
                previousAggregateId,
                clock.instant());
    }
}
