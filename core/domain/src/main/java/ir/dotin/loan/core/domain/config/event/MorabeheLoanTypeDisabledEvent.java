package ir.dotin.loan.core.domain.config.event;

import ir.dotin.loan.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanTypeDisabledEvent(UUID eventId,
                                            Instant createdAt,
                                            MorabeheLoanTypeId aggregateId) implements
        DomainEvent<MorabeheLoanTypeCreatedEvent> {

    public MorabeheLoanTypeDisabledEvent(MorabeheLoanTypeId aggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId);
    }

    public static MorabeheLoanTypeDisabledEvent of(MorabeheLoanTypeId aggregateId) {
        return new MorabeheLoanTypeDisabledEvent(aggregateId);
    }

}
