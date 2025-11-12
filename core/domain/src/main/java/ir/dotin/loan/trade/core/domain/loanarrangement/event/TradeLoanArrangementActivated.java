package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanArrangementActivated(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanArrangementEvents<TradeLoanArrangementActivated, TradeLoanArrangementActivated.Payload> {

    public record Payload() {}

    public TradeLoanArrangementActivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanArrangementActivated of(LoanArrangementId loanArrangementId, Clock clock) {
        return new TradeLoanArrangementActivated(
                randomUUID(),
                loanArrangementId.value(),
                TradeLoanArrangementActivated.class.getSimpleName(),
                TradeLoanArrangementEventType.ACTIVATED.getFullType(),
                new Payload(),
                clock.instant());
    }
}
