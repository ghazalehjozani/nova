package ir.dotin.loan.morabehe.core.domain.config.event;

import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import ir.dotin.platform.ddd.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanTypeDisabledEvent(UUID eventId,
                                            Instant createdAt,
                                            UUID aggregateId) implements DomainEvent<MorabeheLoanTypeDisabledEvent, Object> {

    public MorabeheLoanTypeDisabledEvent(MorabeheLoanTypeId aggregateId) {
        this(TimeBasedUUIDGenerator.generate(), Instant.now(),aggregateId.value());
    }

    public static MorabeheLoanTypeDisabledEvent of(MorabeheLoanTypeId aggregateId) {
        return new MorabeheLoanTypeDisabledEvent(aggregateId);
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
