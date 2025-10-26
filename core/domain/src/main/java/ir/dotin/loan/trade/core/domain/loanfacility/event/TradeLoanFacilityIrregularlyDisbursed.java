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

public record TradeLoanFacilityIrregularlyDisbursed(
        UUID eventId, LoanFacilityId aggregateId, Payload payload, Money amountToDisburse, Instant createdAt)
        implements TradeLoanFacilityEvents<
                TradeLoanFacilityIrregularlyDisbursed, TradeLoanFacilityIrregularlyDisbursed.Payload> {

    public record Payload(SanctionedLoanId sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public TradeLoanFacilityIrregularlyDisbursed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(amountToDisburse);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityIrregularlyDisbursed of(
            LoanFacilityId id, SanctionedLoanId sanId, Money amountToDisburse, Clock clock) {
        return new TradeLoanFacilityIrregularlyDisbursed(
                randomUUID(),
                id,
                new TradeLoanFacilityIrregularlyDisbursed.Payload(sanId),
                amountToDisburse,
                clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "IRREGULARLY_DISBURSED";
    }
}
