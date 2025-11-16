package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanArrangementActivated(UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements TradeLoanArrangementEvents<TradeLoanArrangementActivated> {

    public TradeLoanArrangementActivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanArrangementActivated of(LoanArrangementId loanArrangementId, Clock clock) {
        return new TradeLoanArrangementActivated(
                randomUUID(),
                loanArrangementId.value(),
                TradeLoanArrangementEventType.ACTIVATED.getFullType(),
                clock.instant());
    }
}
