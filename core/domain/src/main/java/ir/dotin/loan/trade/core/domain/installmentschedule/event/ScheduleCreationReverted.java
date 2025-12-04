package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleCreationReverted(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        @Nullable String reason,
        Instant createdAt) implements InstallmentScheduleEvents<ScheduleCreationReverted> {

    public ScheduleCreationReverted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static ScheduleCreationReverted of(InstallmentScheduleId scheduleId, @Nullable String reason, Clock clock) {
        return new ScheduleCreationReverted(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.CREATION_REVERTED.getFullType(),
                reason,
                clock.instant());
    }
}
