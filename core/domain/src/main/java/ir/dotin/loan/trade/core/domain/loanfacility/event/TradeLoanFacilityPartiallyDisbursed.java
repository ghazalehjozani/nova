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

public record TradeLoanFacilityPartiallyDisbursedEvent(
        UUID eventId, LoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<
                TradeLoanFacilityPartiallyDisbursedEvent, TradeLoanFacilityPartiallyDisbursedEvent.Payload> {

    public record Payload(SanctionedLoanId sanctionId, Money totalDisbursedAmount) {
        public Payload {
            requireNonNull(sanctionId);
            requireNonNull(totalDisbursedAmount);
        }
    }

    public TradeLoanFacilityPartiallyDisbursedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityPartiallyDisbursedEvent of(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Money totalDisbursedAmount, Clock clock) {
        return new TradeLoanFacilityPartiallyDisbursedEvent(
                randomUUID(), facilityId, new Payload(sanctionId, totalDisbursedAmount), clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "PARTIALLY_DISBURSED";
    }
}
