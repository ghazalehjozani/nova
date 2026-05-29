package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityContractIssued(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID sanctionedLoanId,
        String transactionNumber,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityContractIssued> {

    public TradeLoanFacilityContractIssued {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionedLoanId);
        requireNonNull(transactionNumber);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityContractIssued of(
            LoanFacilityId id, SanctionedLoanId sanId, String transactionNumber, Clock clock) {
        return new TradeLoanFacilityContractIssued(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.CONTRACT_ISSUED.getFullType(),
                sanId.value(),
                transactionNumber,
                clock.instant());
    }

    public static Builder builder(Clock clock) {
        return new Builder(clock);
    }

    public static final class Builder {
        private @Nullable UUID aggregateId;
        private @Nullable UUID sanctionedLoanId;
        private @Nullable String transactionNumber;
        private @Nullable Instant createdAt;
        private final Clock clock;

        private Builder(Clock clock) {
            this.clock = clock;
        }

        public Builder facilityId(UUID aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder sanctionedLoanId(UUID sanctionedLoanId) {
            this.sanctionedLoanId = sanctionedLoanId;
            return this;
        }

        public Builder transactionNumber(String transactionNumber) {
            this.transactionNumber = transactionNumber;
            return this;
        }

        public Builder occurredAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TradeLoanFacilityContractIssued build() {
            return new TradeLoanFacilityContractIssued(
                    randomUUID(),
                    java.util.Objects.requireNonNull(aggregateId, "aggregateId"),
                    TradeLoanFacilityEventType.CONTRACT_ISSUED.getFullType(),
                    java.util.Objects.requireNonNull(sanctionedLoanId, "sanctionedLoanId"),
                    java.util.Objects.requireNonNull(transactionNumber, "transactionNumber"),
                    createdAt != null ? createdAt : clock.instant());
        }
    }
}
