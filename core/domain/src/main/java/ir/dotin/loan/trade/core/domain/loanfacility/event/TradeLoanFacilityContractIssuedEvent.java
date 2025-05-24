package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

import static java.util.Objects.requireNonNull;

public record TradeLoanFacilityContractIssuedEvent(
        UUID eventId, TradeLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<
                TradeLoanFacilityContractIssuedEvent, TradeLoanFacilityContractIssuedEvent.Payload> {

    public record Payload(TradeSanctionedLoanId sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public TradeLoanFacilityContractIssuedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityContractIssuedEvent of(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanId, Clock clock) {
        return new TradeLoanFacilityContractIssuedEvent(UUID.randomUUID(), id, new Payload(sanId), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CONTRACT_ISSUED";
    }
}
