package ir.dotin.loan.morabehe.core.domain.config.event;

import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanRuleDisabledEvent(UUID eventId,
                                            Instant createdAt,
                                            MorabeheLoanRuleId aggregateId) implements
        DomainEvent<MorabeheLoanRuleDisabledEvent> {
    public MorabeheLoanRuleDisabledEvent(MorabeheLoanRuleId aggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId);
    }

    public static MorabeheLoanRuleDisabledEvent of(MorabeheLoanRuleId aggregateId) {
        return new MorabeheLoanRuleDisabledEvent(aggregateId);
    }
}
