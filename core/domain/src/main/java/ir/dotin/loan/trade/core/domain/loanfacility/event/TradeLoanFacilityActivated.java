package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityActivated(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityActivated, TradeLoanFacilityActivated.Payload> {

    public record Payload(UUID sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public TradeLoanFacilityActivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityActivated of(LoanFacilityId id, SanctionedLoanId sanId, Clock clock) {
        return new TradeLoanFacilityActivated(
                randomUUID(),
                id.value(),
                TradeLoanFacilityActivated.class.getSimpleName(),
                TradeLoanFacilityEventType.ACTIVATED.getFullType(),
                new Payload(sanId.value()),
                clock.instant());
    }
}
