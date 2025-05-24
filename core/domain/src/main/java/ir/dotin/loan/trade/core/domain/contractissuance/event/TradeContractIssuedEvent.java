package ir.dotin.loan.trade.core.domain.contractissuance.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.contractissuance.vo.ContractReference;
import ir.dotin.loan.trade.core.domain.contractissuance.vo.TradeContractIssuanceRecordId;

public record TradeContractIssuedEvent(
        UUID eventId, TradeContractIssuanceRecordId aggregateId, ContractReference payload, Instant createdAt)
        implements TradeContractIssuanceEvent<TradeContractIssuedEvent, ContractReference> {

    public TradeContractIssuedEvent {
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
