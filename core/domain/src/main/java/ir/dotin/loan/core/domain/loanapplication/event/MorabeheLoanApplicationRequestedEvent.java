package ir.dotin.loan.core.domain.loanapplication.event;

import ir.dotin.loan.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanApplicationRequestedEvent(UUID eventId,
                                                    Instant createdAt,
                                                    MorabeheLoanApplicationId aggregateId) implements
        DomainEvent<MorabeheLoanApplicationRequestedEvent> {

    public MorabeheLoanApplicationRequestedEvent(MorabeheLoanApplicationId aggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId);
    }

    public static MorabeheLoanApplicationRequestedEvent of(
            MorabeheLoanApplicationId aggregateId) {
        return new MorabeheLoanApplicationRequestedEvent(aggregateId);
    }

}
