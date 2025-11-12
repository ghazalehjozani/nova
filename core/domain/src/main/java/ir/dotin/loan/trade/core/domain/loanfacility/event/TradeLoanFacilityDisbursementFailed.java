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
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<
                TradeLoanFacilityDisbursementFailed, TradeLoanFacilityDisbursementFailed.Payload> {

    public record Payload(UUID sanctionedLoanId, FailureReason reason) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(reason);
        }
    }

    public TradeLoanFacilityDisbursementFailed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityDisbursementFailed of(
            LoanFacilityId id, SanctionedLoanId sanId, FailureReason reason, Clock clock) {
        return new TradeLoanFacilityDisbursementFailed(
                randomUUID(),
                id.value(),
                TradeLoanFacilityDisbursementFailed.class.getSimpleName(),
                TradeLoanFacilityEventType.DISBURSEMENT_FAILED.getFullType(),
                new Payload(sanId.value(), reason),
                clock.instant());
    }
}
