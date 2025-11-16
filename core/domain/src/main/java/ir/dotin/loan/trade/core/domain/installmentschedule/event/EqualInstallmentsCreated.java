package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record EqualInstallmentsCreated(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        LoanFacilityId loanFacilityId,
        BigDecimal totalAmount,
        int installmentCount,
        Instant createdAt)
        implements InstallmentScheduleEvents<EqualInstallmentsCreated> {

    public EqualInstallmentsCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(loanFacilityId);
        requireNonNull(totalAmount);
        requireNonNull(createdAt);
    }

    public static EqualInstallmentsCreated of(
            InstallmentScheduleId scheduleId,
            LoanFacilityId loanFacilityId,
            BigDecimal totalAmount,
            int installmentCount,
            Clock clock) {
        return new EqualInstallmentsCreated(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.EQUAL_INSTALLMENTS_CREATED.getFullType(),
                loanFacilityId,
                totalAmount,
                installmentCount,
                clock.instant());
    }
}
