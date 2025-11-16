package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanTypeDeactivated(UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements TradeLoanTypeEvents<TradeLoanTypeDeactivated> {

    public TradeLoanTypeDeactivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanTypeDeactivated of(LoanTypeId loanTypeId, Clock clock) {
        return new TradeLoanTypeDeactivated(
                randomUUID(), loanTypeId.value(), TradeLoanTypeEventType.DEACTIVATED.getFullType(), clock.instant());
    }
}
