package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;

public record TradeLoanArrangementDeactivated(
        UUID eventId, TradeLoanArrangementId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanArrangementEvent<TradeLoanArrangementDeactivated, TradeLoanArrangementDeactivated.Payload> {

    public TradeLoanArrangementDeactivated {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(payload, "payload cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "DEACTIVATED";
    }

    public record Payload() {}
}
