package ir.dotin.loan.trade.core.domain.disbursement.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.disbursement.vo.TradeDisbursementRecordId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;

public record TradeDisbursementTransactionPostedEvent(
        UUID eventId, TradeDisbursementRecordId aggregateId, Instant createdAt, Payload payload)
        implements TradeDisbursementEvent<
                TradeDisbursementTransactionPostedEvent, TradeDisbursementTransactionPostedEvent.Payload> {

    public static final String PENDING = "PENDING";

    public TradeDisbursementTransactionPostedEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + PENDING;
    }

    public record Payload(TradeLoanFacilityId loanFacilityId) {
        public Payload {
            Objects.requireNonNull(loanFacilityId, "loanFacilityId cannot be null");
        }
    }

    public static TradeDisbursementTransactionPostedEvent create(
            UUID eventId,
            TradeDisbursementRecordId aggregateId,
            Instant createdAt,
            TradeLoanFacilityId loanFacilityId) {
        return new TradeDisbursementTransactionPostedEvent(
                eventId, aggregateId, createdAt, new TradeDisbursementTransactionPostedEvent.Payload(loanFacilityId));
    }
}
