package ir.dotin.loan.morabehe.core.domain.loanapplication.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;

public record MorabeheLoanApplicationCollateralAddedEvent(UUID eventId, Instant createdAt, UUID aggregateId)
        implements DomainEvent<MorabeheLoanApplicationCollateralAddedEvent, Object> {

    public MorabeheLoanApplicationCollateralAddedEvent(MorabeheLoanApplicationId aggregateId) {
        this(TimeBasedUUIDGenerator.generate(), Instant.now(), aggregateId.value());
    }

    public static MorabeheLoanApplicationCollateralAddedEvent of(MorabeheLoanApplicationId aggregateId) {
        return new MorabeheLoanApplicationCollateralAddedEvent(aggregateId);
    }

    @Override
    public Object payload() {
        return null;
    }

    @Override
    public String eventType() {
        return "";
    }
    // TODO: Implement

}
