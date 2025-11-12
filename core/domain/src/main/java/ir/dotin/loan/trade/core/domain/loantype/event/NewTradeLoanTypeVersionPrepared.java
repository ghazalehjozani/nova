package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record NewTradeLoanTypeVersionPrepared(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanTypeEvents<NewTradeLoanTypeVersionPrepared, NewTradeLoanTypeVersionPrepared.Payload> {

    public record Payload(LoanTypeId newAggregateId, LoanTypeId previousAggregateId) {
        public Payload {
            requireNonNull(newAggregateId);
            requireNonNull(previousAggregateId);
        }
    }

    public NewTradeLoanTypeVersionPrepared {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
        if (!aggregateId.equals(payload.newAggregateId().value()))
            throw new IllegalArgumentException("aggregateId must match payload.newAggregateId");
    }

    public static NewTradeLoanTypeVersionPrepared of(
            LoanTypeId newAggregateId, LoanTypeId previousAggregateId, Clock clock) {
        return new NewTradeLoanTypeVersionPrepared(
                randomUUID(),
                newAggregateId.value(),
                NewTradeLoanTypeVersionPrepared.class.getSimpleName(),
                TradeLoanTypeEventType.VERSION_PREPARED.getFullType(),
                new Payload(newAggregateId, previousAggregateId),
                clock.instant());
    }
}
