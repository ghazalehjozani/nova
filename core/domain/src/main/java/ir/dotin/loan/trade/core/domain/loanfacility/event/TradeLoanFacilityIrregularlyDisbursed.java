package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityIrregularlyDisbursedEvent(
        UUID eventId, LoanFacilityId aggregateId, Payload payload, Money amountToDisburse, Instant createdAt)
        implements TradeLoanFacilityEvent<
        TradeLoanFacilityIrregularlyDisbursedEvent, TradeLoanFacilityIrregularlyDisbursedEvent.Payload> {

    public record Payload(SanctionedLoanId sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public TradeLoanFacilityIrregularlyDisbursedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(amountToDisburse);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityIrregularlyDisbursedEvent of(
            LoanFacilityId id, SanctionedLoanId sanId, Money amountToDisburse, Clock clock) {
        return new TradeLoanFacilityIrregularlyDisbursedEvent(
                randomUUID(),
                id,
                new TradeLoanFacilityIrregularlyDisbursedEvent.Payload(sanId),
                amountToDisburse,
                clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "IRREGULARLY_DISBURSED";
    }
}
