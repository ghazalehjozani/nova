package ir.dotin.loan.morabehe.core.domain.contractissuance.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.morabehe.core.domain.contractissuance.vo.MorabeheContractIssuanceRecordId;

public record MorabeheContractIssuanceFailedEvent(
        UUID eventId, MorabeheContractIssuanceRecordId aggregateId, FailureReason payload, Instant createdAt)
        implements MorabeheContractIssuanceEvent<MorabeheContractIssuanceFailedEvent, FailureReason> {

    public MorabeheContractIssuanceFailedEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(payload, "failureReason cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CONTRACT_ISSUANCE_FAILED";
    }
}
