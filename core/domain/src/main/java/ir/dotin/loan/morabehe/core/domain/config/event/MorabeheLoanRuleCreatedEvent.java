package ir.dotin.loan.morabehe.core.domain.config.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;

public record MorabeheLoanRuleCreatedEvent(UUID eventId, Instant createdAt, UUID aggregateId)
        implements DomainEvent<MorabeheLoanRuleCreatedEvent, String> {

    public MorabeheLoanRuleCreatedEvent(MorabeheLoanRuleId aggregateId) {
        this(TimeBasedUUIDGenerator.generate(), Instant.now(), aggregateId.value());
    }

    public static MorabeheLoanRuleCreatedEvent from(MorabeheLoanRuleId aggregateId) {
        return new MorabeheLoanRuleCreatedEvent(aggregateId);
    }

    @Override
    public String payload() {
        return "payload";
    }

    @Override
    public String eventType() {
        return "MORABEHE_LOAN";
    }
}
