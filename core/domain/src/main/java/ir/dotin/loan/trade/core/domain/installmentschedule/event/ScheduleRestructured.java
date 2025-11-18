package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleRestructured(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        String reason,
        List<UUID> installmentScheduleHistories,
        Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleRestructured> {

    public ScheduleRestructured {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(reason);
        requireNonNull(installmentScheduleHistories);
        requireNonNull(createdAt);
    }

    public static ScheduleRestructured of(
            InstallmentScheduleId scheduleId,
            String reason,
            List<InstallmentScheduleId> installmentScheduleHistories,
            Clock clock) {
        List<@NonNull UUID> uuids = installmentScheduleHistories.stream()
                .map(InstallmentScheduleId::value)
                .toList();
        return new ScheduleRestructured(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.RESTRUCTURED.getFullType(),
                reason,
                uuids,
                clock.instant());
    }
}
