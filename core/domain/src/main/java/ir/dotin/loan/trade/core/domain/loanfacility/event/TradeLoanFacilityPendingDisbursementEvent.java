package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityPendingDisbursementEvent(
        UUID eventId, TradeLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<
                TradeLoanFacilityPendingDisbursementEvent, TradeLoanFacilityPendingDisbursementEvent.Payload> {

    public record Payload(TradeSanctionedLoanId sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public TradeLoanFacilityPendingDisbursementEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityPendingDisbursementEvent of(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanId, Clock clock) {
        return new TradeLoanFacilityPendingDisbursementEvent(randomUUID(), id, new Payload(sanId), clock.instant());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "PENDING_DISBURSEMENT";
    }
}
