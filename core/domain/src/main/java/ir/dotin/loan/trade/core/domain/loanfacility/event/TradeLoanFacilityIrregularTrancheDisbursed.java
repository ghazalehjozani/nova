package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityIrregularlyDisbursed(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID sanctionedLoanId,
        Money amountToDisburse,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityIrregularlyDisbursed> {

    public TradeLoanFacilityIrregularlyDisbursed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionedLoanId);
        requireNonNull(amountToDisburse);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityIrregularlyDisbursed of(
            LoanFacilityId id, SanctionedLoanId sanId, Money amountToDisburse, Clock clock) {
        return new TradeLoanFacilityIrregularlyDisbursed(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.IRREGULARLY_DISBURSED.getFullType(),
                sanId.value(),
                amountToDisburse,
                clock.instant());
    }
}
