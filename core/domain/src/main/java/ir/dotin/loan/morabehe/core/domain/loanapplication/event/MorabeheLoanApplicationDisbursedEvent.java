package ir.dotin.loan.morabehe.core.domain.loanapplication.event;

import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanApplicationDisbursedEvent(UUID eventId,
                                                    Instant createdAt,
                                                    MorabeheLoanApplicationId aggregateId) implements
        DomainEvent<MorabeheLoanApplicationDisbursedEvent> {

    public MorabeheLoanApplicationDisbursedEvent(MorabeheLoanApplicationId aggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId);
    }

    public static MorabeheLoanApplicationDisbursedEvent of(
            MorabeheLoanApplicationId aggregateId) {
        return new MorabeheLoanApplicationDisbursedEvent(aggregateId);
    }

}
