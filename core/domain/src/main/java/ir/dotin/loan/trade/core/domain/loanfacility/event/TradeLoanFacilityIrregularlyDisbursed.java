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
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<
                TradeLoanFacilityIrregularlyDisbursed, TradeLoanFacilityIrregularlyDisbursed.Payload> {

    public record Payload(UUID sanctionedLoanId, Money amountToDisburse) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(amountToDisburse);
        }
    }

    public TradeLoanFacilityIrregularlyDisbursed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityIrregularlyDisbursed of(
            LoanFacilityId id, SanctionedLoanId sanId, Money amountToDisburse, Clock clock) {
        return new TradeLoanFacilityIrregularlyDisbursed(
                randomUUID(),
                id.value(),
                TradeLoanFacilityIrregularlyDisbursed.class.getSimpleName(),
                TradeLoanFacilityEventType.IRREGULARLY_DISBURSED.getFullType(),
                new Payload(sanId.value(), amountToDisburse),
                clock.instant());
    }
}
