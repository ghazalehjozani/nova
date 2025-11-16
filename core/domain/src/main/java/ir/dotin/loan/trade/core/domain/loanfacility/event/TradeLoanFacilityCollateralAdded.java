package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityCollateralAdded(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID sanctionedLoanId,
        String collateralSerial,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityCollateralAdded> {

    public TradeLoanFacilityCollateralAdded {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionedLoanId);
        requireNonNull(collateralSerial);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityCollateralAdded of(
            LoanFacilityId id, SanctionedLoanId sanId, CollateralSerial collateralSerial, Clock clock) {
        return new TradeLoanFacilityCollateralAdded(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.COLLATERAL_ADDED.getFullType(),
                sanId.value(),
                collateralSerial.value(),
                clock.instant());
    }
}
