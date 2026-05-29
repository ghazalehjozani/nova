package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityLumpSumDisbursed(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID sanctionedLoanId,
        List<String> transactionNumbers,
        String installmentPaymentType,
        String applicationNumber,
        @Nullable UUID installmentScheduleId,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityLumpSumDisbursed> {

    public TradeLoanFacilityLumpSumDisbursed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionedLoanId);
        requireNonNull(transactionNumbers);
        requireNonNull(installmentPaymentType);
        requireNonNull(applicationNumber);
        requireNonNull(createdAt);
    }

    public static Builder builder(Clock clock) {
        return new Builder(clock);
    }

    public static class Builder {
        private final Clock clock;
        private @Nullable UUID facilityId;
        private @Nullable UUID sanctionedLoanId;
        private List<String> transactionNumbers = new ArrayList<>();
        private @Nullable String installmentPaymentType;
        private @Nullable String applicationNumber;
        private @Nullable UUID installmentScheduleId;
        private @Nullable Instant occurredAt;

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

        public Builder transactionNumber(String transactionNumber) {
            this.transactionNumbers.add(transactionNumber);
            return this;
        }

        public Builder transactionNumbers(List<String> transactionNumbers) {
            this.transactionNumbers = transactionNumbers;
            return this;
        }

        public Builder installmentPaymentType(String installmentPaymentType) {
            this.installmentPaymentType = installmentPaymentType;
            return this;
        }

        public Builder applicationNumber(String applicationNumber) {
            this.applicationNumber = applicationNumber;
            return this;
        }

        public Builder installmentScheduleId(UUID installmentScheduleId) {
            this.installmentScheduleId = installmentScheduleId;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public TradeLoanFacilityLumpSumDisbursed build() {
            return new TradeLoanFacilityLumpSumDisbursed(
                    UUID.randomUUID(),
                    java.util.Objects.requireNonNull(facilityId, "facilityId"),
                    TradeLoanFacilityEventType.LUMP_SUM_DISBURSED.getFullType(),
                    java.util.Objects.requireNonNull(sanctionedLoanId, "sanctionedLoanId"),
                    transactionNumbers,
                    java.util.Objects.requireNonNull(installmentPaymentType, "installmentPaymentType"),
                    java.util.Objects.requireNonNull(applicationNumber, "applicationNumber"),
                    installmentScheduleId,
                    occurredAt != null ? occurredAt : clock.instant());
        }
    }

    public static TradeLoanFacilityLumpSumDisbursed of(
            LoanFacilityId id,
            SanctionedLoanId sanId,
            InstallmentPaymentType installmentPaymentType,
            ApplicationNumber applicationNumber,
            List<TrackedTransactionNumber> trackedTransactionNumbers,
            @Nullable InstallmentScheduleId installmentScheduleId,
            Clock clock) {
        List<@NonNull String> trxNumbers = trackedTransactionNumbers.stream()
                .map(TrackedTransactionNumber::value)
                .toList();
        UUID scheduleId = installmentScheduleId != null ? installmentScheduleId.value() : null;
        return new TradeLoanFacilityLumpSumDisbursed(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.LUMP_SUM_DISBURSED.getFullType(),
                sanId.value(),
                trxNumbers,
                installmentPaymentType.name(),
                applicationNumber.formattedApplicationNumber(),
                scheduleId,
                clock.instant());
    }
}
