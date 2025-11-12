package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanTypeDeactivated(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanTypeEvents<TradeLoanTypeDeactivated, TradeLoanTypeDeactivated.Payload> {

    public record Payload() {}

    public TradeLoanTypeDeactivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanTypeDeactivated of(LoanTypeId loanTypeId, Clock clock) {
        return new TradeLoanTypeDeactivated(
                randomUUID(),
                loanTypeId.value(),
                TradeLoanTypeDeactivated.class.getSimpleName(),
                TradeLoanTypeEventType.DEACTIVATED.getFullType(),
                new Payload(),
                clock.instant());
    }
}
