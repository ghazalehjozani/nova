package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
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
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<
                TradeLoanFacilityLumpSumDisbursed, TradeLoanFacilityLumpSumDisbursed.Payload> {

    public record Payload(
            UUID sanctionedLoanId,
            List<String> transactionNumbers,
            String installmentPaymentType,
            String applicationNumber,
            UUID installmentScheduleId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(installmentPaymentType);
            requireNonNull(applicationNumber);
            requireNonNull(transactionNumbers);
        }
    }

    public TradeLoanFacilityLumpSumDisbursed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
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
                TradeLoanFacilityLumpSumDisbursed.class.getSimpleName(),
                TradeLoanFacilityEventType.LUMP_SUM_DISBURSED.getFullType(),
                new Payload(
                        sanId.value(),
                        trxNumbers,
                        installmentPaymentType.name(),
                        applicationNumber.formattedApplicationNumber(),
                        scheduleId),
                clock.instant());
    }
}
