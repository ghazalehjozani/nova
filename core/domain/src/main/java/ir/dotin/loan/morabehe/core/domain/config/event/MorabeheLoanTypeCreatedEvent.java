package ir.dotin.loan.morabehe.core.domain.config.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;

public record MorabeheLoanTypeCreatedEvent(UUID eventId, Instant createdAt, UUID aggregateId)
        implements DomainEvent<MorabeheLoanTypeCreatedEvent, Object> {

    public MorabeheLoanTypeCreatedEvent(MorabeheLoanTypeId aggregateId) {
        this(TimeBasedUUIDGenerator.generate(), Instant.now(), aggregateId.value());
    }

    public static MorabeheLoanTypeCreatedEvent of(MorabeheLoanTypeId aggregateId) {
        return new MorabeheLoanTypeCreatedEvent(aggregateId);
    }

    @Override
    public Object payload() {
        return null;
    }

    @Override
    public String eventType() {
        return "";
    }
}
