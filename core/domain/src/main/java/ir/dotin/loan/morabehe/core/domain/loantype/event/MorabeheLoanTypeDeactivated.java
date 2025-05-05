package ir.dotin.loan.morabehe.core.domain.loantype.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.morabehe.core.domain.loantype.vo.MorabeheLoanTypeId;

public record MorabeheLoanTypeDeactivated(
        UUID eventId, MorabeheLoanTypeId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanTypeEvent<MorabeheLoanTypeDeactivated, MorabeheLoanTypeDeactivated.Payload> {

    public MorabeheLoanTypeDeactivated {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(aggregateId);
        Objects.requireNonNull(payload);
    }

    public record Payload() {}

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "DEACTIVATED";
    }
}
