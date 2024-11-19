package ir.dotin.loan.morabehe.core.domain.config.event;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanTypeId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.platform.ddd.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanTypeUpdatedEvent(UUID eventId, Instant createdAt,
                                           MorabeheLoanTypeId aggregateId,
                                           LoanTypeId oldLoanTypeId) implements
        DomainEvent<MorabeheLoanTypeCreatedEvent> {

    public MorabeheLoanTypeUpdatedEvent(MorabeheLoanTypeId aggregateId, LoanTypeId oldAggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId, oldAggregateId);
    }

    public static MorabeheLoanTypeUpdatedEvent of(MorabeheLoanTypeId aggregateId,
                                                  LoanTypeId oldLoanTypeId) {
        return new MorabeheLoanTypeUpdatedEvent(aggregateId, oldLoanTypeId);
    }

}
