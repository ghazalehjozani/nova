package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityOriginationReverted(
        UUID eventId, UUID aggregateId, String eventType, String reason, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityOriginationReverted> {

    public TradeLoanFacilityOriginationReverted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityOriginationReverted of(LoanFacilityId id, String reason, Clock clock) {
        return new TradeLoanFacilityOriginationReverted(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.ORIGINATION_REVERTED.getFullType(),
                reason,
                clock.instant());
    }

    public static Builder builder(Clock clock) {
        return new Builder(clock);
    }

    public static class Builder {
        private final Clock clock;
        private UUID facilityId;
        private String reason;
        private Instant occurredAt;

        public Builder(Clock clock) {
            this.clock = clock;
        }

        public Builder facilityId(UUID facilityId) {
            this.facilityId = facilityId;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public TradeLoanFacilityOriginationReverted build() {
            return new TradeLoanFacilityOriginationReverted(
                    randomUUID(),
                    facilityId,
                    TradeLoanFacilityEventType.ORIGINATION_REVERTED.getFullType(),
                    reason,
                    occurredAt != null ? occurredAt : clock.instant());
        }
    }
}
