package ir.dotin.loan.trade.core.domain.contractissuance.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.trade.core.domain.contractissuance.vo.TradeContractIssuanceRecordId;

public record TradeContractIssuanceFailedEvent(
        UUID eventId, TradeContractIssuanceRecordId aggregateId, FailureReason payload, Instant createdAt)
        implements TradeContractIssuanceEvent<TradeContractIssuanceFailedEvent, FailureReason> {

    public TradeContractIssuanceFailedEvent {
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
