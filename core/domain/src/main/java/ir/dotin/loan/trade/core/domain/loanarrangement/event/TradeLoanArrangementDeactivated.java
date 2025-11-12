package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanArrangementDeactivated(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanArrangementEvents<
                TradeLoanArrangementDeactivated, TradeLoanArrangementDeactivated.Payload> {

    public record Payload() {}

    public TradeLoanArrangementDeactivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanArrangementDeactivated of(LoanArrangementId loanArrangementId, Clock clock) {
        return new TradeLoanArrangementDeactivated(
                randomUUID(),
                loanArrangementId.value(),
                TradeLoanArrangementDeactivated.class.getSimpleName(),
                TradeLoanArrangementEventType.DEACTIVATED.getFullType(),
                new Payload(),
                clock.instant());
    }
}
