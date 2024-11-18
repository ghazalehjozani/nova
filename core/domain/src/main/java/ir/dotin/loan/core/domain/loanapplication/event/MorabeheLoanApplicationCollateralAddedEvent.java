package ir.dotin.loan.core.domain.loanapplication.event;

import ir.dotin.loan.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanApplicationCollateralAddedEvent(UUID eventId, Instant createdAt,
                                                          MorabeheLoanApplicationId aggregateId) implements
        DomainEvent<MorabeheLoanApplicationCollateralAddedEvent> {

    public MorabeheLoanApplicationCollateralAddedEvent(MorabeheLoanApplicationId aggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId);
    }

    public static MorabeheLoanApplicationCollateralAddedEvent of(
            MorabeheLoanApplicationId aggregateId) {
        return new MorabeheLoanApplicationCollateralAddedEvent(aggregateId);
    }

}
