package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityRestructuring(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        String restructuringTransaction,
        Integer newDuration,
        UUID scheduleId,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityRestructuring> {

    public TradeLoanFacilityRestructuring {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityRestructuring of(
            LoanFacilityId facilityId,
            String restructuringTransaction,
            InstallmentScheduleId scheduleId,
            Integer newDuration,
            Clock clock) {
        return new TradeLoanFacilityRestructuring(
                randomUUID(),
                facilityId.value(),
                TradeLoanFacilityEventType.RESTRUCTURING.getFullType(),
                restructuringTransaction,
                newDuration,
                scheduleId.value(),
                clock.instant());
    }
}
