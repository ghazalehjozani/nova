package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityIrregularDisbursementEvent(
        UUID eventId, TradeLoanFacilityId aggregateId, Payload payload, Money amountToDisburse, Instant createdAt)
        implements TradeLoanFacilityEvent<
                TradeLoanFacilityIrregularDisbursementEvent, TradeLoanFacilityIrregularDisbursementEvent.Payload> {

    public record Payload(TradeSanctionedLoanId sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public TradeLoanFacilityIrregularDisbursementEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(amountToDisburse);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityIrregularDisbursementEvent of(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanId, Money amountToDisburse, Clock clock) {
        return new TradeLoanFacilityIrregularDisbursementEvent(
                randomUUID(),
                id,
                new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanId),
                amountToDisburse,
                clock.instant());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "IRREGULAR_DISBURSEMENT";
    }
}
