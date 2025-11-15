package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityPartiallyDisbursed(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID sanctionedLoanId,
        List<String> trackedTransactionNumbers,
        String trancheAmount,
        String totalDisbursed,
        String remainingCapacity,
        int trancheNumber,
        UUID installmentScheduleId,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityPartiallyDisbursed> {

    public TradeLoanFacilityPartiallyDisbursed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionedLoanId);
        requireNonNull(trackedTransactionNumbers);
        requireNonNull(trancheAmount);
        requireNonNull(totalDisbursed);
        requireNonNull(remainingCapacity);
        requireNonNull(installmentScheduleId);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityPartiallyDisbursed of(
            LoanFacilityId facilityId,
            SanctionedLoanId sanctionedLoanId,
            List<TrackedTransactionNumber> trackedNumbers,
            String trancheAmount,
            String totalDisbursed,
            String remainingCapacity,
            int trancheNumber,
            InstallmentScheduleId scheduleId,
            Clock clock) {
        List<String> trxNumbers =
                trackedNumbers.stream().map(TrackedTransactionNumber::value).toList();

        return new TradeLoanFacilityPartiallyDisbursed(
                randomUUID(),
                facilityId.value(),
                TradeLoanFacilityEventType.PARTIALLY_DISBURSED.getFullType(),
                sanctionedLoanId.value(),
                trxNumbers,
                trancheAmount,
                totalDisbursed,
                remainingCapacity,
                trancheNumber,
                scheduleId.value(),
                clock.instant());
    }
}
