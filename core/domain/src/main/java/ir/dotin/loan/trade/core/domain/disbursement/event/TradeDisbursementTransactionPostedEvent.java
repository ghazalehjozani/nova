package ir.dotin.loan.trade.core.domain.disbursement.event;

import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.disbursement.vo.TradeDisbursementRecordId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;

import static java.util.Objects.requireNonNull;

public record TradeDisbursementTransactionPostedEvent(
        UUID eventId, TradeDisbursementRecordId aggregateId, Instant createdAt, Payload payload)
        implements TradeDisbursementEvent<
                TradeDisbursementTransactionPostedEvent, TradeDisbursementTransactionPostedEvent.Payload> {

    public static final String PENDING = "PENDING";

    public TradeDisbursementTransactionPostedEvent {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null");
        requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + PENDING;
    }

    public record Payload(TradeLoanFacilityId loanFacilityId) {
        public Payload {
            requireNonNull(loanFacilityId, "loanFacilityId cannot be null");
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
