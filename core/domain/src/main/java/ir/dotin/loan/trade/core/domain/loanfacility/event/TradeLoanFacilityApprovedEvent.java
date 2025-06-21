package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityApprovedEvent(
        UUID eventId, TradeLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<TradeLoanFacilityApprovedEvent, TradeLoanFacilityApprovedEvent.Payload> {

    public record Payload(TradeSanctionedLoanId sanctionedLoanId, SanctionSerial sanctionSerial) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(sanctionSerial);
        }
    }

    public TradeLoanFacilityApprovedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityApprovedEvent of(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanId, SanctionSerial sanctionSerial, Clock clock) {
        return new TradeLoanFacilityApprovedEvent(
                randomUUID(), id, new Payload(sanId, sanctionSerial), clock.instant());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "APPROVED";
    }
}
