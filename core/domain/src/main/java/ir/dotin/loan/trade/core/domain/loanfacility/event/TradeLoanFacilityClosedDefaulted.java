package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityClosedDefaulted(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityClosedDefaulted, TradeLoanFacilityClosedDefaulted.Payload> {

    public record Payload(UUID sanctionedLoanId) {
        public Payload {
            requireNonNull(sanctionedLoanId);
        }
    }

    public TradeLoanFacilityClosedDefaulted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityClosedDefaulted of(LoanFacilityId id, SanctionedLoanId sanId, Clock clock) {
        return new TradeLoanFacilityClosedDefaulted(
                randomUUID(),
                id.value(),
                TradeLoanFacilityClosedDefaulted.class.getSimpleName(),
                TradeLoanFacilityEventType.CLOSED_DEFAULTED.getFullType(),
                new Payload(sanId.value()),
                clock.instant());
    }
}
