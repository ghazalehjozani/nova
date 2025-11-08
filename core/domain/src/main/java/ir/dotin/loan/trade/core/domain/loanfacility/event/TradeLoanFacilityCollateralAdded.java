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

public record TradeLoanFacilityCollateralAdded(
        UUID eventId, LoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityCollateralAdded, TradeLoanFacilityCollateralAdded.Payload> {

    public record Payload(UUID loanFacilityId, UUID sanctionedLoanId, String collateralSerial) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(collateralSerial);
        }
    }

    public TradeLoanFacilityCollateralAdded {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityCollateralAdded of(
            LoanFacilityId id, SanctionedLoanId sanId, CollateralSerial collateralSerial, Clock clock) {
        return new TradeLoanFacilityCollateralAdded(
                randomUUID(), id, new Payload(id.value(), sanId.value(), collateralSerial.value()), clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "COLLATERAL_ADDED";
    }
}
