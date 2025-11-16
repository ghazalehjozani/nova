package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityRejected(
        UUID eventId, UUID aggregateId, String eventType, UUID applicationId, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityRejected> {

    public TradeLoanFacilityRejected {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(applicationId);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityRejected of(LoanFacilityId id, LoanApplicationId appId, Clock clock) {
        return new TradeLoanFacilityRejected(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.REJECTED.getFullType(),
                appId.value(),
                clock.instant());
    }
}
