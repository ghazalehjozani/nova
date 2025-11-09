package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityApprovalSubmitted(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<
                TradeLoanFacilityApprovalSubmitted, TradeLoanFacilityApprovalSubmitted.Payload> {

    public record Payload(UUID applicationId) {
        public Payload {
            requireNonNull(applicationId);
        }
    }

    public TradeLoanFacilityApprovalSubmitted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityApprovalSubmitted of(LoanFacilityId id, LoanApplicationId appId, Clock clock) {
        return new TradeLoanFacilityApprovalSubmitted(
                randomUUID(),
                id.value(),
                TradeLoanFacilityApprovalSubmitted.class.getSimpleName(),
                TradeLoanFacilityEventType.PENDING_APPROVAL.getFullType(),
                new Payload(appId.value()),
                clock.instant());
    }
}
