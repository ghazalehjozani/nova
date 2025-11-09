package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanArrangementCreated(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanArrangementEvents<TradeLoanArrangementCreated, TradeLoanArrangementCreated.Payload> {

    public record Payload(LoanArrangementId loanArrangementId) {
        public Payload {
            requireNonNull(loanArrangementId);
        }
    }

    public TradeLoanArrangementCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanArrangementCreated of(LoanArrangementId loanArrangementId, Clock clock) {
        return new TradeLoanArrangementCreated(
                randomUUID(),
                loanArrangementId.value(),
                TradeLoanArrangementCreated.class.getSimpleName(),
                TradeLoanArrangementEventType.CREATED.getFullType(),
                new Payload(loanArrangementId),
                clock.instant());
    }
}
