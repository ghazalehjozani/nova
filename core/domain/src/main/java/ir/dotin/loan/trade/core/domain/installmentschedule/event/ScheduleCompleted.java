package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleCompleted(UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleCompleted> {

    public ScheduleCompleted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static ScheduleCompleted of(InstallmentScheduleId scheduleId, Clock clock) {
        return new ScheduleCompleted(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.COMPLETED.getFullType(),
                clock.instant());
    }
}
