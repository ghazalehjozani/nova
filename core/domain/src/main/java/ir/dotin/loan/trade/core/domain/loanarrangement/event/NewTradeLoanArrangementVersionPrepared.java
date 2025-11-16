package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record NewTradeLoanArrangementVersionPrepared(
        UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements TradeLoanArrangementEvents<NewTradeLoanArrangementVersionPrepared> {

    public NewTradeLoanArrangementVersionPrepared {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static NewTradeLoanArrangementVersionPrepared of(LoanArrangementId newVersionId, Clock clock) {
        return new NewTradeLoanArrangementVersionPrepared(
                randomUUID(),
                newVersionId.value(),
                TradeLoanArrangementEventType.VERSION_PREPARED.getFullType(),
                clock.instant());
    }
}
