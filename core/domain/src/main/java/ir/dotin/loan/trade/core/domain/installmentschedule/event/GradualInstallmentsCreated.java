package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record GradualInstallmentsCreated(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID loanFacilityId,
        String status,
        BigDecimal totalAmount,
        List<UUID> installmentIds,
        Instant createdAt)
        implements InstallmentScheduleEvents<GradualInstallmentsCreated> {

    public GradualInstallmentsCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(loanFacilityId);
        requireNonNull(status);
        requireNonNull(totalAmount);
        requireNonNull(installmentIds);
        requireNonNull(createdAt);
    }

    public static GradualInstallmentsCreated of(
            InstallmentScheduleId scheduleId,
            InstallmentScheduleStatus installmentScheduleStatus,
            LoanFacilityId loanFacilityId,
            BigDecimal totalAmount,
            List<InstallmentId> specifications,
            Clock clock) {
        List<@NonNull UUID> installmentIds =
                specifications.stream().map(InstallmentId::value).toList();
        return new GradualInstallmentsCreated(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.GRADUAL_INSTALLMENTS_CREATED.getFullType(),
                loanFacilityId.value(),
                installmentScheduleStatus.name(),
                totalAmount,
                installmentIds,
                clock.instant());
    }
}
