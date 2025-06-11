package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

import static java.util.Objects.requireNonNull;

public record TradeLoanTypeDeactivated(UUID eventId, TradeLoanTypeId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanTypeEvent<TradeLoanTypeDeactivated, TradeLoanTypeDeactivated.Payload> {

    public TradeLoanTypeDeactivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
    }

    public record Payload() {}

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "DEACTIVATED";
    }
}
