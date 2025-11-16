package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleCancelled(UUID eventId, UUID aggregateId, String eventType, String reason, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleCancelled> {

    public ScheduleCancelled {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(reason);
        requireNonNull(createdAt);
    }

    public static ScheduleCancelled of(InstallmentScheduleId scheduleId, String reason, Clock clock) {
        return new ScheduleCancelled(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.CANCELLED.getFullType(),
                reason,
                clock.instant());
    }
}
