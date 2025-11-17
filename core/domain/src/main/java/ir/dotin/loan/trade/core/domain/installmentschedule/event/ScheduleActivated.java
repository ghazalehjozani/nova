package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleActivated(
        UUID eventId, UUID aggregateId, String eventType, List<UUID> installmentScheduleHistories, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleActivated> {

    public ScheduleActivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static ScheduleActivated of(
            InstallmentScheduleId scheduleId, List<InstallmentScheduleId> installmentScheduleHistories, Clock clock) {
        List<@NonNull UUID> uuids = installmentScheduleHistories.stream()
                .map(InstallmentScheduleId::value)
                .toList();
        return new ScheduleActivated(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.ACTIVATED.getFullType(),
                uuids,
                clock.instant());
    }
}
