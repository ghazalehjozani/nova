package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityApprovalReverted(UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityApprovalReverted> {

    public TradeLoanFacilityApprovalReverted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityApprovalReverted of(LoanFacilityId id, Clock clock) {
        return new TradeLoanFacilityApprovalReverted(
                randomUUID(), id.value(), TradeLoanFacilityEventType.APPROVAL_REVERTED.getFullType(), clock.instant());
    }

    public static Builder builder(Clock clock) {
        return new Builder(clock);
    }

    public static class Builder {
        private final Clock clock;
        private @Nullable UUID facilityId;
        private @Nullable Instant occurredAt;

        public Builder(Clock clock) {
            this.clock = clock;
        }

        public Builder facilityId(UUID facilityId) {
            this.facilityId = facilityId;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public TradeLoanFacilityApprovalReverted build() {
            return new TradeLoanFacilityApprovalReverted(
                    randomUUID(),
                    java.util.Objects.requireNonNull(facilityId, "facilityId"),
                    TradeLoanFacilityEventType.APPROVAL_REVERTED.getFullType(),
                    occurredAt != null ? occurredAt : clock.instant());
        }
    }
}
