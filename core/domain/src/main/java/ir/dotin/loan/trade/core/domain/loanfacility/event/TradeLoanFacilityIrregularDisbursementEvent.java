package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityIrregularDisbursementEvent(
        UUID eventId, LoanFacilityId aggregateId, Payload payload, Money amountToDisburse, Instant createdAt)
        implements TradeLoanFacilityEvent<
                TradeLoanFacilityIrregularDisbursementEvent, TradeLoanFacilityIrregularDisbursementEvent.Payload> {

    public record Payload(SanctionedLoanId sanctionedLoanId) {
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
            LoanFacilityId id, SanctionedLoanId sanId, Money amountToDisburse, Clock clock) {
        return new TradeLoanFacilityIrregularDisbursementEvent(
                randomUUID(),
                id,
                new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanId),
                amountToDisburse,
                clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "IRREGULAR_DISBURSEMENT";
    }
}
