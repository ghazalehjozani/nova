package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;

import static java.util.Objects.requireNonNull;

public record TradeLoanArrangementActivated(
        UUID eventId, TradeLoanArrangementId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanArrangementEvent<TradeLoanArrangementActivated, TradeLoanArrangementActivated.Payload> {

    public TradeLoanArrangementActivated {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null");
        requireNonNull(payload, "payload cannot be null");
        requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "ACTIVATED";
    }

    public record Payload() {}
}
