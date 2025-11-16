package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanTypeCreated(UUID eventId, UUID aggregateId, String eventType, String code, Instant createdAt)
        implements TradeLoanTypeEvents<TradeLoanTypeCreated> {

    public TradeLoanTypeCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(code);
        requireNonNull(createdAt);
    }

    public static TradeLoanTypeCreated of(LoanTypeId loanTypeId, String code, Clock clock) {
        return new TradeLoanTypeCreated(
                randomUUID(), loanTypeId.value(), TradeLoanTypeEventType.CREATED.getFullType(), code, clock.instant());
    }
}
