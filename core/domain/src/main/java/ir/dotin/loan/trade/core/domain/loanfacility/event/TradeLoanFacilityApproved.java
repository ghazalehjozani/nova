package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityApproved(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID sanctionedLoanId,
        String sanctionSerial,
        SanctionType sanctionType,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityApproved> {

    public TradeLoanFacilityApproved {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionedLoanId);
        requireNonNull(sanctionSerial);
        requireNonNull(sanctionType);
        requireNonNull(createdAt);
    }

    public static Builder builder(Clock clock) {
        return new Builder(clock);
    }

    public static class Builder {
        private final Clock clock;
        private UUID facilityId;
        private UUID sanctionedLoanId;
        private String sanctionSerial;
        private SanctionType sanctionType;
        private Instant occurredAt;

        public Builder(Clock clock) {
            this.clock = clock;
        }

        public Builder facilityId(UUID facilityId) {
            this.facilityId = facilityId;
            return this;
        }

        public Builder sanctionedLoanId(UUID sanctionedLoanId) {
            this.sanctionedLoanId = sanctionedLoanId;
            return this;
        }

        public Builder sanctionSerial(String sanctionSerial) {
            this.sanctionSerial = sanctionSerial;
            return this;
        }

        public Builder sanctionType(SanctionType sanctionType) {
            this.sanctionType = sanctionType;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public TradeLoanFacilityApproved build() {
            return new TradeLoanFacilityApproved(
                    UUID.randomUUID(),
                    facilityId,
                    TradeLoanFacilityEventType.APPROVED.getFullType(),
                    sanctionedLoanId,
                    sanctionSerial,
                    sanctionType,
                    occurredAt != null ? occurredAt : clock.instant());
        }
    }

    public static TradeLoanFacilityApproved of(
            LoanFacilityId id, SanctionedLoanId sanId, String sanctionSerial, SanctionType sanctionType, Clock clock) {
        return new TradeLoanFacilityApproved(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.APPROVED.getFullType(),
                sanId.value(),
                sanctionSerial,
                sanctionType,
                clock.instant());
    }
}
