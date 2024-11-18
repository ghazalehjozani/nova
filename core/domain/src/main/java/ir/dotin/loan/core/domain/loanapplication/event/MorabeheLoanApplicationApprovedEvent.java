package ir.dotin.loan.core.domain.loanapplication.event;

import ir.dotin.loan.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanApplicationApprovedEvent(UUID eventId,
                                                   Instant createdAt,
                                                   MorabeheLoanApplicationId aggregateId) implements
        DomainEvent<MorabeheLoanApplicationApprovedEvent> {

    public MorabeheLoanApplicationApprovedEvent(MorabeheLoanApplicationId aggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId);
    }

    public static MorabeheLoanApplicationApprovedEvent of(MorabeheLoanApplicationId aggregateId) {
        return new MorabeheLoanApplicationApprovedEvent(aggregateId);
    }

}
