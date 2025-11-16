package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanArrangementCreated(
        UUID eventId, UUID aggregateId, String eventType, LoanArrangementId loanArrangementId, Instant createdAt)
        implements TradeLoanArrangementEvents<TradeLoanArrangementCreated> {

    public TradeLoanArrangementCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(loanArrangementId);
        requireNonNull(createdAt);
    }

    public static TradeLoanArrangementCreated of(LoanArrangementId loanArrangementId, Clock clock) {
        return new TradeLoanArrangementCreated(
                randomUUID(),
                loanArrangementId.value(),
                TradeLoanArrangementEventType.CREATED.getFullType(),
                loanArrangementId,
                clock.instant());
    }
}
