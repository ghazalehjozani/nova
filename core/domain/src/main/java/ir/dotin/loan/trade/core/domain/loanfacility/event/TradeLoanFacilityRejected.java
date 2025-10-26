package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityRejected(UUID eventId, LoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityRejected, TradeLoanFacilityRejected.Payload> {

    public record Payload(LoanApplicationId applicationId) {
        public Payload {
            requireNonNull(applicationId);
        }
    }

    public TradeLoanFacilityRejected {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityRejected of(LoanFacilityId id, LoanApplicationId appId, Clock clock) {
        return new TradeLoanFacilityRejected(randomUUID(), id, new Payload(appId), clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "REJECTED";
    }
}
