package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityIrregularTrancheDisbursementReverted(
        UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityIrregularTrancheDisbursementReverted> {

    public TradeLoanFacilityIrregularTrancheDisbursementReverted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityIrregularTrancheDisbursementReverted of(LoanFacilityId id, Clock clock) {
        return new TradeLoanFacilityIrregularTrancheDisbursementReverted(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.IRREGULAR_TRANCHE_DISBURSEMENT_REVERTED.getFullType(),
                clock.instant());
    }

    public static Builder builder(Clock clock) {
        return new Builder(clock);
    }

    public static class Builder {
        private final Clock clock;
        private UUID facilityId;
        private Instant occurredAt;

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

        public TradeLoanFacilityIrregularTrancheDisbursementReverted build() {
            return new TradeLoanFacilityIrregularTrancheDisbursementReverted(
                    randomUUID(),
                    facilityId,
                    TradeLoanFacilityEventType.IRREGULAR_TRANCHE_DISBURSEMENT_REVERTED.getFullType(),
                    occurredAt != null ? occurredAt : clock.instant());
        }
    }
}
