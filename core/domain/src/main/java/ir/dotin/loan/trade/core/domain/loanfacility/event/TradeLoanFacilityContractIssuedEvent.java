package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityContractIssuedEvent(
        UUID eventId, TradeLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<
                TradeLoanFacilityContractIssuedEvent, TradeLoanFacilityContractIssuedEvent.Payload> {

    public record Payload(TradeSanctionedLoanId sanctionedLoanId, List<TransactionNumber> transactionNumbers) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(transactionNumbers);
        }
    }

    public TradeLoanFacilityContractIssuedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityContractIssuedEvent of(
            TradeLoanFacilityId id,
            TradeSanctionedLoanId sanId,
            List<TransactionNumber> transactionNumbers,
            Clock clock) {
        return new TradeLoanFacilityContractIssuedEvent(
                randomUUID(), id, new Payload(sanId, transactionNumbers), clock.instant());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CONTRACT_ISSUED";
    }
}
