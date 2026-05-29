package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

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

    public static Builder builder(Clock clock) {
        return new Builder(clock);
    }

    public static class Builder {
        private final Clock clock;
        private @Nullable UUID facilityId;
        private @Nullable UUID applicationId;
        private @Nullable Instant occurredAt;

        public Builder(Clock clock) {
            this.clock = clock;
        }

        public Builder facilityId(UUID facilityId) {
            this.facilityId = facilityId;
            return this;
        }

        public Builder applicationId(UUID applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public TradeLoanFacilityApprovalSubmitted build() {
            return new TradeLoanFacilityApprovalSubmitted(
                    UUID.randomUUID(),
                    java.util.Objects.requireNonNull(facilityId, "facilityId"),
                    TradeLoanFacilityEventType.APPROVAL_SUBMITTED.getFullType(),
                    java.util.Objects.requireNonNull(applicationId, "applicationId"),
                    occurredAt != null ? occurredAt : clock.instant());
        }
    }

    public static TradeLoanFacilityApprovalSubmitted of(LoanFacilityId id, LoanApplicationId appId, Clock clock) {
        return new TradeLoanFacilityApprovalSubmitted(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.APPROVAL_SUBMITTED.getFullType(),
                appId.value(),
                clock.instant());
    }
}
