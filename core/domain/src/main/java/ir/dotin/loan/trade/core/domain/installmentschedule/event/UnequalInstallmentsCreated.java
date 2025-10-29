package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record UnequalInstallmentsCreated(
        UUID eventId, InstallmentScheduleId aggregateId, Payload payload, Instant createdAt)
        implements InstallmentScheduleEvents<UnequalInstallmentsCreated, UnequalInstallmentsCreated.Payload> {

    public record Payload(LoanFacilityId loanFacilityId, BigDecimal totalAmount, List<InstallmentId> specifications) {
        public Payload {
            requireNonNull(loanFacilityId);
            requireNonNull(totalAmount);
            requireNonNull(specifications);
        }
    }

    public UnequalInstallmentsCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static UnequalInstallmentsCreated of(
            InstallmentScheduleId scheduleId,
            LoanFacilityId loanFacilityId,
            BigDecimal totalAmount,
            List<InstallmentId> specifications,
            Clock clock) {
        return new UnequalInstallmentsCreated(
                randomUUID(), scheduleId, new Payload(loanFacilityId, totalAmount, specifications), clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "UNEQUAL_INSTALLMENTS_CREATED";
    }
}
