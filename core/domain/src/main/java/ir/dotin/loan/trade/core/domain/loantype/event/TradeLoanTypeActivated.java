package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;

public record TradeLoanTypeActivated(UUID eventId, LoanTypeId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanTypeEvents<TradeLoanTypeActivated, TradeLoanTypeActivated.Payload> {

    public TradeLoanTypeActivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
    }

    public record Payload() {}

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "ACTIVATED";
    }
}
