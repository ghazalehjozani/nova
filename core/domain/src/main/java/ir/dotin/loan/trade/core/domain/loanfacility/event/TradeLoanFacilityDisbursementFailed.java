package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityDisbursementFailed(
        UUID eventId, LoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<
                TradeLoanFacilityDisbursementFailed, TradeLoanFacilityDisbursementFailed.Payload> {

    public record Payload(UUID loanFacilityId, UUID sanctionedLoanId, FailureReason reason) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(reason);
        }
    }

    public TradeLoanFacilityDisbursementFailed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityDisbursementFailed of(
            LoanFacilityId id, SanctionedLoanId sanId, FailureReason reason, Clock clock) {
        return new TradeLoanFacilityDisbursementFailed(
                randomUUID(), id, new Payload(id.value(), sanId.value(), reason), clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "DISBURSEMENT_FAILED";
    }
}
