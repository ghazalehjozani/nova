package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanTypeActivated(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanTypeEvents<TradeLoanTypeActivated, TradeLoanTypeActivated.Payload> {

    public record Payload() {}

    public TradeLoanTypeActivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanTypeActivated of(LoanTypeId loanTypeId, Clock clock) {
        return new TradeLoanTypeActivated(
                randomUUID(),
                loanTypeId.value(),
                TradeLoanTypeActivated.class.getSimpleName(),
                TradeLoanTypeEventType.ACTIVATED.getFullType(),
                new Payload(),
                clock.instant());
    }
}
