package ir.dotin.loan.trade.core.domain.contractissuance.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.contractissuance.vo.TradeContractIssuanceRecordId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;

import static java.util.Objects.requireNonNull;

public record TradeContractIssuanceTransactionPostedEvent(
        UUID eventId, TradeContractIssuanceRecordId aggregateId, Instant createdAt, Payload payload)
        implements TradeContractIssuanceEvent<
                TradeContractIssuanceTransactionPostedEvent, TradeContractIssuanceTransactionPostedEvent.Payload> {

    public TradeContractIssuanceTransactionPostedEvent {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null");
        requireNonNull(createdAt, "createdAt cannot be null");
    }

    public record Payload(TradeLoanFacilityId loanFacilityId) {

        public Payload {
            requireNonNull(loanFacilityId, "loanFacilityId cannot be null");
        }
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CONTRACT_ISSUANCE_CREATED";
    }

    public static TradeContractIssuanceTransactionPostedEvent create(
            UUID eventId,
            TradeContractIssuanceRecordId aggregateId,
            Instant createdAt,
            TradeLoanFacilityId loanFacilityId) {
        return new TradeContractIssuanceTransactionPostedEvent(
                eventId,
                aggregateId,
                createdAt,
                new TradeContractIssuanceTransactionPostedEvent.Payload(loanFacilityId));
    }
}
