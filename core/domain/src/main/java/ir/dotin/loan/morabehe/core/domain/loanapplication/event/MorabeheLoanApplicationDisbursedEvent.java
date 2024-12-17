package ir.dotin.loan.morabehe.core.domain.loanapplication.event;

import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import ir.dotin.platform.ddd.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanApplicationDisbursedEvent(UUID eventId,
                                                    Instant createdAt,
                                                    UUID aggregateId) implements
        DomainEvent<MorabeheLoanApplicationDisbursedEvent, Object> {

    public MorabeheLoanApplicationDisbursedEvent(MorabeheLoanApplicationId aggregateId) {
        this(TimeBasedUUIDGenerator.generate(), Instant.now(),aggregateId.value());
    }

    public static MorabeheLoanApplicationDisbursedEvent of(
            MorabeheLoanApplicationId aggregateId) {
        return new MorabeheLoanApplicationDisbursedEvent(aggregateId);
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
