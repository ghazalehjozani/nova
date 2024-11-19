package ir.dotin.loan.morabehe.core.domain.loanapplication.event;

import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanApplicationContractIssuedEvent(UUID eventId,
                                                         Instant createdAt,
                                                         MorabeheLoanApplicationId aggregateId) implements
        DomainEvent<MorabeheLoanApplicationContractIssuedEvent> {

    public MorabeheLoanApplicationContractIssuedEvent(MorabeheLoanApplicationId aggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId);
    }

    public static MorabeheLoanApplicationContractIssuedEvent of(
            MorabeheLoanApplicationId aggregateId) {
        return new MorabeheLoanApplicationContractIssuedEvent(aggregateId);
    }

}
