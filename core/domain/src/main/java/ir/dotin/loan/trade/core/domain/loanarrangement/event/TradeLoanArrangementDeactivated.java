package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import static java.util.Objects.requireNonNull;

public record TradeLoanArrangementDeactivated(
        UUID eventId, LoanArrangementId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanArrangementEvents<
                TradeLoanArrangementDeactivated, TradeLoanArrangementDeactivated.Payload> {

    public TradeLoanArrangementDeactivated {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null");
        requireNonNull(payload, "payload cannot be null");
        requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "DEACTIVATED";
    }

    public record Payload() {}
}
