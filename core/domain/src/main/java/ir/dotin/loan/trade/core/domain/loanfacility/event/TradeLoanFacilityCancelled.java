package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityCancelled(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityCancelled, TradeLoanFacilityCancelled.Payload> {

    public record Payload(Optional<UUID> sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public TradeLoanFacilityCancelled {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityCancelled of(LoanFacilityId id, Clock clock) {
        return new TradeLoanFacilityCancelled(
                randomUUID(),
                id.value(),
                TradeLoanFacilityCancelled.class.getSimpleName(),
                TradeLoanFacilityEventType.CANCELLED.getFullType(),
                new Payload(Optional.empty()),
                clock.instant());
    }

    public static TradeLoanFacilityCancelled of(LoanFacilityId id, SanctionedLoanId sanId, Clock clock) {
        return new TradeLoanFacilityCancelled(
                randomUUID(),
                id.value(),
                TradeLoanFacilityCancelled.class.getSimpleName(),
                TradeLoanFacilityEventType.CANCELLED.getFullType(),
                new Payload(Optional.of(sanId.value())),
                clock.instant());
    }
}
