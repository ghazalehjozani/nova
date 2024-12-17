package ir.dotin.loan.morabehe.core.domain.config.event;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanTypeId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.platform.ddd.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanTypeUpdatedEvent(String eventId, Instant createdAt,
                                           String aggregateId,
                                           LoanTypeId oldLoanTypeId) implements DomainEvent {

    public MorabeheLoanTypeUpdatedEvent(MorabeheLoanTypeId aggregateId, LoanTypeId oldAggregateId) {
        this(UUID.randomUUID().toString(), Instant.now(), aggregateId.value().toString(), oldAggregateId);
    }

    public static MorabeheLoanTypeUpdatedEvent of(MorabeheLoanTypeId aggregateId,
                                                  LoanTypeId oldLoanTypeId) {
        return new MorabeheLoanTypeUpdatedEvent(aggregateId, oldLoanTypeId);
    }
//TODO: Implement

    @Override
    public String eventType() {
        return "";
    }

    @Override
    public Object payload() {
        return null;
    }

    @Override
    public String correlationId() {
        return "";
    }

    @Override
    public String useCaseId() {
        return "";
    }
}
