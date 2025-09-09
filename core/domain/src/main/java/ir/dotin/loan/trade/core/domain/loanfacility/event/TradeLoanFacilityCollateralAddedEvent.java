package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityCollateralAddedEvent(
        UUID eventId, LoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvent<
                TradeLoanFacilityCollateralAddedEvent, TradeLoanFacilityCollateralAddedEvent.Payload> {

    public record Payload(SanctionedLoanId sanctionedLoanId, CollateralSerial collateralSerial) {
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
            LoanFacilityId id, SanctionedLoanId sanId, CollateralSerial collateralSerial, Clock clock) {
        return new TradeLoanFacilityCollateralAddedEvent(
                randomUUID(), id, new Payload(sanId, collateralSerial), clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "COLLATERAL_ADDED";
    }
}
