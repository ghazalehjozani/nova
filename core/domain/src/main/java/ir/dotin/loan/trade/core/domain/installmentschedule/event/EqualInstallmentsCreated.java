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
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements InstallmentScheduleEvents<EqualInstallmentsCreated, EqualInstallmentsCreated.Payload> {

    public record Payload(LoanFacilityId loanFacilityId, BigDecimal totalAmount, int installmentCount) {
        public Payload {
            requireNonNull(loanFacilityId);
            requireNonNull(totalAmount);
        }
    }

    public EqualInstallmentsCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
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
                EqualInstallmentsCreated.class.getSimpleName(),
                InstallmentScheduleEventType.EQUAL_INSTALLMENTS_CREATED.getFullType(),
                new Payload(loanFacilityId, totalAmount, installmentCount),
                clock.instant());
    }
}
