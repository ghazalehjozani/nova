package ir.dotin.loan.morabehe.core.domain.config.event;

import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanTypeCreatedEvent(UUID eventId,
                                           Instant createdAt,
                                           MorabeheLoanTypeId aggregateId) implements
        DomainEvent<MorabeheLoanTypeCreatedEvent> {

    public MorabeheLoanTypeCreatedEvent(MorabeheLoanTypeId aggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId);
    }

    public static MorabeheLoanTypeCreatedEvent of(MorabeheLoanTypeId aggregateId) {
        return new MorabeheLoanTypeCreatedEvent(aggregateId);
    }

}
