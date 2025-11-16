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
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID sanctionId,
        Money totalDisbursedAmount,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityAdditionalDisbursementCompleted> {

    public TradeLoanFacilityAdditionalDisbursementCompleted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionId);
        requireNonNull(totalDisbursedAmount);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityAdditionalDisbursementCompleted of(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Money totalDisbursedAmount, Clock clock) {
        return new TradeLoanFacilityAdditionalDisbursementCompleted(
                randomUUID(),
                facilityId.value(),
                TradeLoanFacilityEventType.ADDITIONAL_DISBURSEMENT_COMPLETED.getFullType(),
                sanctionId.value(),
                totalDisbursedAmount,
                clock.instant());
    }
}
