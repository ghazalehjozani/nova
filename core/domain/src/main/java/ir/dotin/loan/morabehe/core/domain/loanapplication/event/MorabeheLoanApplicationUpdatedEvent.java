package ir.dotin.loan.morabehe.core.domain.loanapplication.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;

public record MorabeheLoanApplicationUpdatedEvent(UUID eventId, Instant createdAt, UUID aggregateId)
        implements DomainEvent<MorabeheLoanApplicationUpdatedEvent, Object> {

    public MorabeheLoanApplicationUpdatedEvent(MorabeheLoanApplicationId aggregateId) {
        this(TimeBasedUUIDGenerator.generate(), Instant.now(), aggregateId.value());
    }

    public static MorabeheLoanApplicationUpdatedEvent of(MorabeheLoanApplicationId aggregateId) {
        return new MorabeheLoanApplicationUpdatedEvent(aggregateId);
    }

    @Override
    public Object payload() {
        return null;
    }

    @Override
    public String eventType() {
        return "";
    }
    // TODO: Implement

}
