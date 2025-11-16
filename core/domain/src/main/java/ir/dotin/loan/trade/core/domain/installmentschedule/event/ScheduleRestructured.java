package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleRestructured(
        UUID eventId, UUID aggregateId, String eventType, String reason, Map<String, Object> changes, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleRestructured> {

    public ScheduleRestructured {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(reason);
        requireNonNull(changes);
        requireNonNull(createdAt);
    }

    public static ScheduleRestructured of(
            InstallmentScheduleId scheduleId, String reason, Map<String, Object> changes, Clock clock) {
        return new ScheduleRestructured(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.RESTRUCTURED.getFullType(),
                reason,
                changes,
                clock.instant());
    }
}
