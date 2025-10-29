package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record EqualInstallmentsCreated(
        UUID eventId, InstallmentScheduleId aggregateId, Payload payload, Instant createdAt)
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
                randomUUID(), scheduleId, new Payload(loanFacilityId, totalAmount, installmentCount), clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "EQUAL_INSTALLMENTS_CREATED";
    }
}
