package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import static java.util.Objects.requireNonNull;

public record TradeLoanArrangementCreated(
        UUID eventId, LoanArrangementId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanArrangementEvent<TradeLoanArrangementCreated, TradeLoanArrangementCreated.Payload> {

    public static final String CREATED = "CREATED";

    public TradeLoanArrangementCreated {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null");
        requireNonNull(payload, "payload cannot be null");
        requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + CREATED;
    }

    public record Payload(LoanArrangementId loanArrangementId) {
        public Payload {
            requireNonNull(loanArrangementId);
        }
    }
}
