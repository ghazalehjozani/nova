package ir.dotin.loan.morabehe.core.domain.loanapplication.event;

import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MorabeheLoanApplicationApprovedEvent(String eventId,
                                                   Instant createdAt,
                                                   String aggregateId) implements
        DomainEvent {

    public MorabeheLoanApplicationApprovedEvent(MorabeheLoanApplicationId aggregateId) {
        this(UUID.randomUUID().toString(), Instant.now(),aggregateId.value().toString());
    }

    public static MorabeheLoanApplicationApprovedEvent of(MorabeheLoanApplicationId aggregateId) {
        return new MorabeheLoanApplicationApprovedEvent(aggregateId);
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
