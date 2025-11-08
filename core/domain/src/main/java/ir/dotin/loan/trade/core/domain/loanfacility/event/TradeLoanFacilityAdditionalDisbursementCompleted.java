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

public record TradeLoanFacilityAdditionalDisbursementCompleted(
        UUID eventId, LoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<
                TradeLoanFacilityAdditionalDisbursementCompleted,
                TradeLoanFacilityAdditionalDisbursementCompleted.Payload> {

    public record Payload(UUID loanFacilityId, UUID sanctionId, Money totalDisbursedAmount) {
        public Payload {
            requireNonNull(sanctionId);
            requireNonNull(totalDisbursedAmount);
        }
    }

    public TradeLoanFacilityAdditionalDisbursementCompleted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityAdditionalDisbursementCompleted of(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Money totalDisbursedAmount, Clock clock) {
        return new TradeLoanFacilityAdditionalDisbursementCompleted(
                randomUUID(),
                facilityId,
                new Payload(facilityId.value(), sanctionId.value(), totalDisbursedAmount),
                clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "ADDITIONAL_DISBURSEMENT_COMPLETED";
    }
}
