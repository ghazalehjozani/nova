package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityContractIssued(
        UUID eventId, LoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityContractIssued, TradeLoanFacilityContractIssued.Payload> {

    public record Payload(SanctionedLoanId sanctionedLoanId, String transactionNumber) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(transactionNumber);
        }
    }

    public TradeLoanFacilityContractIssued {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityContractIssued of(
            LoanFacilityId id, SanctionedLoanId sanId, String transactionNumber, Clock clock) {
        return new TradeLoanFacilityContractIssued(
                randomUUID(), id, new Payload(sanId, transactionNumber), clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "CONTRACT_ISSUED";
    }
}
