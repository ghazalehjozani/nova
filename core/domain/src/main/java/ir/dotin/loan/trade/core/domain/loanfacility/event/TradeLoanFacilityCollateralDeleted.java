package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityCollateralDeleted(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID sanctionedLoanId,
        List<String> deletedSerials,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityCollateralDeleted> {

    public TradeLoanFacilityCollateralDeleted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionedLoanId);
        requireNonNull(deletedSerials);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityCollateralDeleted of(
            LoanFacilityId id, SanctionedLoanId sanId, List<CollateralSerial> deletedSerials, Clock clock) {
        List<String> serialNumbers =
                deletedSerials.stream().map(CollateralSerial::value).toList();
        return new TradeLoanFacilityCollateralDeleted(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.COLLATERAL_DELETED.getFullType(),
                sanId.value(),
                serialNumbers,
                clock.instant());
    }
}
