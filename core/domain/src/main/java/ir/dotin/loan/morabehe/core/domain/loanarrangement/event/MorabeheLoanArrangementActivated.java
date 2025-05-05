package ir.dotin.loan.morabehe.core.domain.loanarrangement.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.morabehe.core.domain.loanarrangement.vo.MorabeheLoanArrangementId;

public record MorabeheLoanArrangementActivated(
        UUID eventId, MorabeheLoanArrangementId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanArrangementEvent<
                MorabeheLoanArrangementActivated, MorabeheLoanArrangementActivated.Payload> {

    public MorabeheLoanArrangementActivated {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(payload, "payload cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "ACTIVATED";
    }

    public record Payload() {}
}
