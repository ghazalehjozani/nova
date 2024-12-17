package ir.dotin.loan.morabehe.core.domain.config.event;

import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import ir.dotin.platform.ddd.common.event.DomainEvent;

import java.time.Instant;

public record MorabeheLoanRuleCreatedEvent(String eventId,
                                           Instant createdAt,
                                           String aggregateId) implements DomainEvent {

    public MorabeheLoanRuleCreatedEvent(MorabeheLoanRuleId aggregateId) {
        this(TimeBasedUUIDGenerator.generate().toString(), Instant.now(), aggregateId.value().toString());
    }

    public static MorabeheLoanRuleCreatedEvent from(MorabeheLoanRuleId aggregateId) {
        return new MorabeheLoanRuleCreatedEvent(aggregateId);
    }

    @Override
    public String eventType() {
        return "eventType";
    }

    @Override
    public Object payload() {
        return "null";
    }

    @Override
    public String correlationId() {
        return "correlationId";
    }

    @Override
    public String useCaseId() {
        return "useCaseId";
    }
}
