package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityFullyDisbursed(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID sanctionedLoanId,
        String totalDisbursed,
        int totalTranches,
        List<String> trackedTransactionNumbers,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityFullyDisbursed> {

    public TradeLoanFacilityFullyDisbursed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionedLoanId);
        requireNonNull(totalDisbursed);
        requireNonNull(trackedTransactionNumbers);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityFullyDisbursed of(
            LoanFacilityId facilityId,
            SanctionedLoanId sanctionedLoanId,
            String totalDisbursed,
            int totalTranches,
            List<TrackedTransactionNumber> trackedNumbers,
            Clock clock) {
        List<String> trxNumbers =
                trackedNumbers.stream().map(TrackedTransactionNumber::value).toList();
        return new TradeLoanFacilityFullyDisbursed(
                randomUUID(),
                facilityId.value(),
                TradeLoanFacilityEventType.FULLY_DISBURSED.getFullType(),
                sanctionedLoanId.value(),
                totalDisbursed,
                totalTranches,
                trxNumbers,
                clock.instant());
    }
}
