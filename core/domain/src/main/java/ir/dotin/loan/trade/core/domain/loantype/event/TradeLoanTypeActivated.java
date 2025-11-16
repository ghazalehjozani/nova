package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanTypeActivated(UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements TradeLoanTypeEvents<TradeLoanTypeActivated> {

    public TradeLoanTypeActivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanTypeActivated of(LoanTypeId loanTypeId, Clock clock) {
        return new TradeLoanTypeActivated(
                randomUUID(), loanTypeId.value(), TradeLoanTypeEventType.ACTIVATED.getFullType(), clock.instant());
    }
}
