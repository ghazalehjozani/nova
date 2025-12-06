package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record InstallmentCreationReverted(
        UUID eventId, UUID aggregateId, String eventType, List<UUID> removedInstallmentIds, Instant createdAt)
        implements InstallmentScheduleEvents<InstallmentCreationReverted> {

    public InstallmentCreationReverted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(removedInstallmentIds);
        requireNonNull(createdAt);
        removedInstallmentIds = List.copyOf(removedInstallmentIds);
    }

    public static InstallmentCreationReverted of(
            InstallmentScheduleId scheduleId, List<InstallmentId> removedInstallmentIds, Clock clock) {
        List<@NonNull UUID> uuids =
                removedInstallmentIds.stream().map(InstallmentId::value).toList();
        return new InstallmentCreationReverted(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.INSTALLMENT_CREATION_REVERTED.getFullType(),
                uuids,
                clock.instant());
    }
}
