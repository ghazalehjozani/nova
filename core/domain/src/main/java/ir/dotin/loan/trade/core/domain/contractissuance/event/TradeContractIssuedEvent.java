package ir.dotin.loan.trade.core.domain.contractissuance.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.contractissuance.vo.ContractReference;
import ir.dotin.loan.trade.core.domain.contractissuance.vo.TradeContractIssuanceRecordId;

import static java.util.Objects.requireNonNull;

public record TradeContractIssuedEvent(
        UUID eventId, TradeContractIssuanceRecordId aggregateId, ContractReference payload, Instant createdAt)
        implements TradeContractIssuanceEvent<TradeContractIssuedEvent, ContractReference> {

    public TradeContractIssuedEvent {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null");
        requireNonNull(payload, "ContractReference payload cannot be null");
        requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CONTRACT_ISSUED";
    }
}
