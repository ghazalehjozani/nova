package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanTypeGroupRemoved(UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements TradeLoanTypeEvents<TradeLoanTypeGroupRemoved> {

    public TradeLoanTypeGroupRemoved {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanTypeGroupRemoved of(LoanTypeId loanTypeId, Clock clock) {
        return new TradeLoanTypeGroupRemoved(
                randomUUID(), loanTypeId.value(), TradeLoanTypeEventType.GROUP_REMOVED.getFullType(), clock.instant());
    }
}
