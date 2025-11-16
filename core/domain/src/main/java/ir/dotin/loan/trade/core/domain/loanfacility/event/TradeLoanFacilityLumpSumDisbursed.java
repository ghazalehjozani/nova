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
