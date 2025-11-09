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
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleStateTransitioned, ScheduleStateTransitioned.Payload> {

    public record Payload(
            InstallmentScheduleStatus fromStatus, InstallmentScheduleStatus toStatus, @Nullable String reason) {
        public Payload {
            requireNonNull(fromStatus);
            requireNonNull(toStatus);
        }
    }

    public ScheduleStateTransitioned {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
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
                ScheduleStateTransitioned.class.getSimpleName(),
                InstallmentScheduleEventType.STATE_TRANSITIONED.getFullType(),
                new Payload(fromStatus, toStatus, reason),
                clock.instant());
    }
}
