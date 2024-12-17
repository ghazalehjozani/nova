package ir.dotin.loan.morabehe.core.domain.config.event;

import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.platform.ddd.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanTypeDisabledEvent(String eventId,
                                            Instant createdAt,
                                            String aggregateId) implements DomainEvent {

    public MorabeheLoanTypeDisabledEvent(MorabeheLoanTypeId aggregateId) {
        this(UUID.randomUUID().toString(), Instant.now(),aggregateId.value().toString());
    }

    public static MorabeheLoanTypeDisabledEvent of(MorabeheLoanTypeId aggregateId) {
        return new MorabeheLoanTypeDisabledEvent(aggregateId);
    }

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
