package ir.dotin.loan.morabehe.core.domain.config.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;

public record MorabeheLoanRuleDisabledEvent(UUID eventId, Instant createdAt, UUID aggregateId)
        implements DomainEvent<MorabeheLoanRuleDisabledEvent, Object> {
    public MorabeheLoanRuleDisabledEvent(MorabeheLoanRuleId aggregateId) {
        this(TimeBasedUUIDGenerator.generate(), Instant.now(), aggregateId.value());
    }

    public static MorabeheLoanRuleDisabledEvent of(MorabeheLoanRuleId aggregateId) {
        return new MorabeheLoanRuleDisabledEvent(aggregateId);
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
