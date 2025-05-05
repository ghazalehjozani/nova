package ir.dotin.loan.morabehe.core.domain.contractissuance.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.morabehe.core.domain.contractissuance.vo.MorabeheContractIssuanceRecordId;

public record MorabeheContractIssuancePendingEvent(
        UUID eventId, MorabeheContractIssuanceRecordId aggregateId, Instant createdAt)
        implements MorabeheContractIssuanceEvent<MorabeheContractIssuancePendingEvent, Void> {

    public MorabeheContractIssuancePendingEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public Void payload() {
        return null;
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "GENERATION_PENDING";
    }
}
