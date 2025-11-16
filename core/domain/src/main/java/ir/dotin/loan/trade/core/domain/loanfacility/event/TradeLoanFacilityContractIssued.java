package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

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
}
