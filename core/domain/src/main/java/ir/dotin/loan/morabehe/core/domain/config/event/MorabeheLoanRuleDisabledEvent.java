package ir.dotin.loan.morabehe.core.domain.config.event;

import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanRuleDisabledEvent(String eventId,
                                            Instant createdAt,
                                            String aggregateId) implements DomainEvent {
    public MorabeheLoanRuleDisabledEvent(MorabeheLoanRuleId aggregateId) {
        this(UUID.randomUUID().toString(), Instant.now(),aggregateId.value().toString());
    }

    public static MorabeheLoanRuleDisabledEvent of(MorabeheLoanRuleId aggregateId) {
        return new MorabeheLoanRuleDisabledEvent(aggregateId);
    }
//TODO: Implement

    @Override
    public String eventType() {
        return "";
    }

    @Override
    public Object payload() {
        return null;
    }

    @Override
    public String correlationId() {
        return "";
    }

    @Override
    public String useCaseId() {
        return "";
    }
}
