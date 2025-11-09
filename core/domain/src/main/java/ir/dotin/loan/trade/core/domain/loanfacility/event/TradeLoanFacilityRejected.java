package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityRejected(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityRejected, TradeLoanFacilityRejected.Payload> {

    public record Payload(UUID applicationId) {
        public Payload {
            requireNonNull(applicationId);
        }
    }

    public TradeLoanFacilityRejected {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityRejected of(LoanFacilityId id, LoanApplicationId appId, Clock clock) {
        return new TradeLoanFacilityRejected(
                randomUUID(),
                id.value(),
                TradeLoanFacilityRejected.class.getSimpleName(),
                TradeLoanFacilityEventType.REJECTED.getFullType(),
                new Payload(appId.value()),
                clock.instant());
    }
}
