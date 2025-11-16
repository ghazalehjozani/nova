package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityDisbursementFailed(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID sanctionedLoanId,
        FailureReason reason,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityDisbursementFailed> {

    public TradeLoanFacilityDisbursementFailed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionedLoanId);
        requireNonNull(reason);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityDisbursementFailed of(
            LoanFacilityId id, SanctionedLoanId sanId, FailureReason reason, Clock clock) {
        return new TradeLoanFacilityDisbursementFailed(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.DISBURSEMENT_FAILED.getFullType(),
                sanId.value(),
                reason,
                clock.instant());
    }
}
