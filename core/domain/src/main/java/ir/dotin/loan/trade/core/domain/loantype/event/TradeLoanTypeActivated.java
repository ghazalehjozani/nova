package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

public record TradeLoanTypeActivated(UUID eventId, TradeLoanTypeId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanTypeEvent<TradeLoanTypeActivated, TradeLoanTypeActivated.Payload> {

    public TradeLoanTypeActivated {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(aggregateId);
        Objects.requireNonNull(payload);
    }

    public record Payload() {}

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "ACTIVATED";
    }
}
