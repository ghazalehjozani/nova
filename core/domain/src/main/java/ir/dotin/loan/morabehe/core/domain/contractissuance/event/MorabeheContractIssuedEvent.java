package ir.dotin.loan.morabehe.core.domain.contractissuance.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.contractissuance.vo.ContractReference;
import ir.dotin.loan.morabehe.core.domain.contractissuance.vo.MorabeheContractIssuanceRecordId;

public record MorabeheContractIssuedEvent(
        UUID eventId, MorabeheContractIssuanceRecordId aggregateId, ContractReference payload, Instant createdAt)
        implements MorabeheContractIssuanceEvent<MorabeheContractIssuedEvent, ContractReference> {

    public MorabeheContractIssuedEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(payload, "ContractReference payload cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CONTRACT_ISSUED";
    }
}
