package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record NewTradeLoanArrangementVersionPrepared(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Instant createdAt)
        implements TradeLoanArrangementEvents<NewTradeLoanArrangementVersionPrepared, Void> {

    public NewTradeLoanArrangementVersionPrepared {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    @Override
    public Void payload() {
        return null;
    }

    public static NewTradeLoanArrangementVersionPrepared of(LoanArrangementId newVersionId, Clock clock) {
        return new NewTradeLoanArrangementVersionPrepared(
                randomUUID(),
                newVersionId.value(),
                NewTradeLoanArrangementVersionPrepared.class.getSimpleName(),
                TradeLoanArrangementEventType.VERSION_PREPARED.getFullType(),
                clock.instant());
    }
}
