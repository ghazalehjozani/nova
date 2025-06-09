package ir.dotin.loan.trade.core.domain.contractissuance.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.trade.core.domain.contractissuance.vo.TradeContractIssuanceRecordId;

import static java.util.Objects.requireNonNull;

public record TradeContractIssuanceFailedEvent(
        UUID eventId, TradeContractIssuanceRecordId aggregateId, FailureReason payload, Instant createdAt)
        implements TradeContractIssuanceEvent<TradeContractIssuanceFailedEvent, FailureReason> {

    public TradeContractIssuanceFailedEvent {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null");
        requireNonNull(payload, "failureReason cannot be null");
        requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CONTRACT_ISSUANCE_FAILED";
    }
}
