package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleRestructured(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleRestructured, ScheduleRestructured.Payload> {

    public record Payload(String reason, Map<String, Object> changes) {
        public Payload {
            requireNonNull(reason);
            requireNonNull(changes);
        }
    }

    public ScheduleRestructured {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static ScheduleRestructured of(
            InstallmentScheduleId scheduleId, String reason, Map<String, Object> changes, Clock clock) {
        return new ScheduleRestructured(
                randomUUID(),
                scheduleId.value(),
                ScheduleRestructured.class.getSimpleName(),
                InstallmentScheduleEventType.RESTRUCTURED.getFullType(),
                new Payload(reason, changes),
                clock.instant());
    }
}
