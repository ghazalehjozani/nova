package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityApprovalSubmitted(
        UUID eventId, UUID aggregateId, String eventType, UUID applicationId, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityApprovalSubmitted> {

    public TradeLoanFacilityApprovalSubmitted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(applicationId);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityApprovalSubmitted of(LoanFacilityId id, LoanApplicationId appId, Clock clock) {
        return new TradeLoanFacilityApprovalSubmitted(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.PENDING_APPROVAL.getFullType(),
                appId.value(),
                clock.instant());
    }
}
