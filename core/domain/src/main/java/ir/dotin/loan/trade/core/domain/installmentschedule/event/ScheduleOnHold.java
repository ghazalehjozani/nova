package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleOnHold(UUID eventId, UUID aggregateId, String eventType, String reason, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleOnHold> {

    public ScheduleOnHold {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(reason);
        requireNonNull(createdAt);
    }

    public static ScheduleOnHold of(InstallmentScheduleId scheduleId, String reason, Clock clock) {
        return new ScheduleOnHold(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.ON_HOLD.getFullType(),
                reason,
                clock.instant());
    }
}
