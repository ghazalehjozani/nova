package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanTypeCreated(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanTypeEvents<TradeLoanTypeCreated, TradeLoanTypeCreated.Payload> {

    public record Payload(String code) {
        public Payload {
            requireNonNull(code);
        }
    }

    public TradeLoanTypeCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanTypeCreated of(LoanTypeId loanTypeId, String code, Clock clock) {
        return new TradeLoanTypeCreated(
                randomUUID(),
                loanTypeId.value(),
                TradeLoanTypeCreated.class.getSimpleName(),
                TradeLoanTypeEventType.CREATED.getFullType(),
                new Payload(code),
                clock.instant());
    }
}
