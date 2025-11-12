package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityContractIssued(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityContractIssued, TradeLoanFacilityContractIssued.Payload> {

    public record Payload(UUID sanctionedLoanId, String transactionNumber) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(transactionNumber);
        }
    }

    public TradeLoanFacilityContractIssued {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityContractIssued of(
            LoanFacilityId id, SanctionedLoanId sanId, String transactionNumber, Clock clock) {
        return new TradeLoanFacilityContractIssued(
                randomUUID(),
                id.value(),
                TradeLoanFacilityContractIssued.class.getSimpleName(),
                TradeLoanFacilityEventType.CONTRACT_ISSUED.getFullType(),
                new Payload(sanId.value(), transactionNumber),
                clock.instant());
    }
}
