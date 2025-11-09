package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityAdditionalDisbursementCompleted(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<
                TradeLoanFacilityAdditionalDisbursementCompleted,
                TradeLoanFacilityAdditionalDisbursementCompleted.Payload> {

    public record Payload(UUID sanctionId, Money totalDisbursedAmount) {
        public Payload {
            requireNonNull(sanctionId);
            requireNonNull(totalDisbursedAmount);
        }
    }

    public TradeLoanFacilityAdditionalDisbursementCompleted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityAdditionalDisbursementCompleted of(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Money totalDisbursedAmount, Clock clock) {
        return new TradeLoanFacilityAdditionalDisbursementCompleted(
                randomUUID(),
                facilityId.value(),
                TradeLoanFacilityAdditionalDisbursementCompleted.class.getSimpleName(),
                TradeLoanFacilityEventType.ADDITIONAL_DISBURSEMENT_COMPLETED.getFullType(),
                new Payload(sanctionId.value(), totalDisbursedAmount),
                clock.instant());
    }
}
