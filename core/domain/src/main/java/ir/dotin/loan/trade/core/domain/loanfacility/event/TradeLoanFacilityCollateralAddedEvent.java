package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

import static java.util.Objects.requireNonNull;

public record TradeLoanFacilityCollateralAddedEvent(
        UUID eventId, TradeLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<
                TradeLoanFacilityCollateralAddedEvent, TradeLoanFacilityCollateralAddedEvent.Payload> {

    public record Payload(TradeSanctionedLoanId sanctionedLoanId, CollateralSerial collateralSerial) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(collateralSerial);
        }
    }

    public TradeLoanFacilityCollateralAddedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityCollateralAddedEvent of(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanId, CollateralSerial collateralSerial, Clock clock) {
        return new TradeLoanFacilityCollateralAddedEvent(
                UUID.randomUUID(), id, new Payload(sanId, collateralSerial), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "COLLATERAL_ADDED";
    }
}
