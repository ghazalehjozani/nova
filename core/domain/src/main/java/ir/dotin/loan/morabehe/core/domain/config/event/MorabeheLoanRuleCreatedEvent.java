package ir.dotin.loan.morabehe.core.domain.config.event;

import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanRuleCreatedEvent(UUID eventId,
                                           Instant createdAt,
                                           MorabeheLoanRuleId aggregateId) implements
        DomainEvent<MorabeheLoanRuleCreatedEvent> {

    public MorabeheLoanRuleCreatedEvent(MorabeheLoanRuleId aggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId);
    }

    public static MorabeheLoanRuleCreatedEvent of(MorabeheLoanRuleId aggregateId) {
        return new MorabeheLoanRuleCreatedEvent(aggregateId);
    }
}
