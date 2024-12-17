package ir.dotin.loan.morabehe.core.domain.config.event;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanTypeId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import ir.dotin.platform.ddd.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanTypeUpdatedEvent(UUID eventId, Instant createdAt,
                                           UUID aggregateId,
                                           LoanTypeId oldLoanTypeId) implements DomainEvent <MorabeheLoanTypeUpdatedEvent, Object>{

    public MorabeheLoanTypeUpdatedEvent(MorabeheLoanTypeId aggregateId, LoanTypeId oldAggregateId) {
        this(TimeBasedUUIDGenerator.generate(), Instant.now(), aggregateId.value(), oldAggregateId);
    }

    public static MorabeheLoanTypeUpdatedEvent of(MorabeheLoanTypeId aggregateId,
                                                  LoanTypeId oldLoanTypeId) {
        return new MorabeheLoanTypeUpdatedEvent(aggregateId, oldLoanTypeId);
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
