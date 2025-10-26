package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import static java.util.Objects.requireNonNull;

public record NewTradeLoanArrangementVersionPrepared(UUID eventId, LoanArrangementId aggregateId, Instant createdAt)
        implements TradeLoanArrangementEvents<NewTradeLoanArrangementVersionPrepared, Void> {

    public NewTradeLoanArrangementVersionPrepared {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null (should be new version ID)");
        requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "VERSION_PREPARED";
    }

    @Override
    public Void payload() {
        return null;
    }
}
