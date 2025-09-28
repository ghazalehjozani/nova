package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;

public record NewTradeLoanTypeVersionPrepared(UUID eventId, LoanTypeId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanTypeEvent<NewTradeLoanTypeVersionPrepared, NewTradeLoanTypeVersionPrepared.Payload> {

    public NewTradeLoanTypeVersionPrepared {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(createdAt);
        if (!aggregateId.equals(payload.newAggregateId()))
            throw new IllegalArgumentException("aggregateId must match payload.newAggregateId");
    }

    public record Payload(LoanTypeId newAggregateId, LoanTypeId previousAggregateId) {
        public Payload {
            requireNonNull(newAggregateId);
            requireNonNull(previousAggregateId);
        }
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "VERSION_PREPARED";
    }
}
