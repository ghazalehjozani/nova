package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleActivated(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleActivated, ScheduleActivated.Payload> {

    public record Payload() {}

    public ScheduleActivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static ScheduleActivated of(InstallmentScheduleId scheduleId, Clock clock) {
        return new ScheduleActivated(
                randomUUID(),
                scheduleId.value(),
                ScheduleActivated.class.getSimpleName(),
                InstallmentScheduleEventType.ACTIVATED.getFullType(),
                new Payload(),
                clock.instant());
    }
}
