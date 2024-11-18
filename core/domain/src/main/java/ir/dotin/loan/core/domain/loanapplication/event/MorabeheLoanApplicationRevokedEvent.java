package ir.dotin.loan.core.domain.loanapplication.event;

import ir.dotin.loan.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanApplicationRevokedEvent(UUID eventId,
                                                  Instant createdAt,
                                                  MorabeheLoanApplicationId aggregateId) implements
        DomainEvent<MorabeheLoanApplicationRevokedEvent> {


    public MorabeheLoanApplicationRevokedEvent(MorabeheLoanApplicationId aggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId);
    }

    public static MorabeheLoanApplicationRevokedEvent of(
            MorabeheLoanApplicationId aggregateId) {
        return new MorabeheLoanApplicationRevokedEvent(aggregateId);
    }

}
