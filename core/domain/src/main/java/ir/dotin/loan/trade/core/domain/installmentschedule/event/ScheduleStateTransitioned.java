package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleStateTransitioned(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        InstallmentScheduleStatus fromStatus,
        InstallmentScheduleStatus toStatus,
        @Nullable String reason,
        Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleStateTransitioned> {

    public ScheduleStateTransitioned {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(fromStatus);
        requireNonNull(toStatus);
        requireNonNull(createdAt);
    }

    public static ScheduleStateTransitioned of(
            InstallmentScheduleId scheduleId,
            InstallmentScheduleStatus fromStatus,
            InstallmentScheduleStatus toStatus,
            @Nullable String reason,
            Clock clock) {
        return new ScheduleStateTransitioned(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.STATE_TRANSITIONED.getFullType(),
                fromStatus,
                toStatus,
                reason,
                clock.instant());
    }
}
