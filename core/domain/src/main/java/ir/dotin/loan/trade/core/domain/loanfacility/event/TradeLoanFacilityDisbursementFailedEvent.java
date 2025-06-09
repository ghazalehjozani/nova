package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityDisbursementFailedEvent(
        UUID eventId, TradeLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<
                TradeLoanFacilityDisbursementFailedEvent, TradeLoanFacilityDisbursementFailedEvent.Payload> {

    public record Payload(TradeSanctionedLoanId sanctionedLoanId, FailureReason reason) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(reason);
        }
    }

    public TradeLoanFacilityDisbursementFailedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityDisbursementFailedEvent of(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanId, FailureReason reason, Clock clock) {
        return new TradeLoanFacilityDisbursementFailedEvent(
                randomUUID(), id, new Payload(sanId, reason), clock.instant());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "DISBURSEMENT_FAILED";
    }
}
