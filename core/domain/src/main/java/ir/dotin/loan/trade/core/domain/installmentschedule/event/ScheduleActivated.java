package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleActivated(UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleActivated> {

    public ScheduleActivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static ScheduleActivated of(InstallmentScheduleId scheduleId, Clock clock) {
        return new ScheduleActivated(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.ACTIVATED.getFullType(),
                clock.instant());
    }
}
