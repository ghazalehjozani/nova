package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityPartiallyDisbursed(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<
                TradeLoanFacilityPartiallyDisbursed, TradeLoanFacilityPartiallyDisbursed.Payload> {

    public record Payload(UUID sanctionId, Money totalDisbursedAmount) {
        public Payload {
            requireNonNull(sanctionId);
            requireNonNull(totalDisbursedAmount);
        }
    }

    public TradeLoanFacilityPartiallyDisbursed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityPartiallyDisbursed of(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Money totalDisbursedAmount, Clock clock) {
        return new TradeLoanFacilityPartiallyDisbursed(
                randomUUID(),
                facilityId.value(),
                TradeLoanFacilityPartiallyDisbursed.class.getSimpleName(),
                TradeLoanFacilityEventType.PARTIALLY_DISBURSED.getFullType(),
                new Payload(sanctionId.value(), totalDisbursedAmount),
                clock.instant());
    }
}
