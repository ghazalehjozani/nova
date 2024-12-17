package ir.dotin.loan.morabehe.core.domain.loanapplication.event;

import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import ir.dotin.platform.ddd.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanApplicationRequestedEvent(UUID eventId,
                                                    Instant createdAt,
                                                    UUID aggregateId) implements
        DomainEvent<MorabeheLoanApplicationRequestedEvent, Object> {

    public MorabeheLoanApplicationRequestedEvent(MorabeheLoanApplicationId aggregateId) {
        this(TimeBasedUUIDGenerator.generate(), Instant.now(),aggregateId.value());
    }

    public static MorabeheLoanApplicationRequestedEvent of(
            MorabeheLoanApplicationId aggregateId) {
        return new MorabeheLoanApplicationRequestedEvent(aggregateId);
    }

    @Override
    public Object payload() {
        return null;
    }

    @Override
    public String eventType() {
        return "";
    }
//TODO: Implement


}
