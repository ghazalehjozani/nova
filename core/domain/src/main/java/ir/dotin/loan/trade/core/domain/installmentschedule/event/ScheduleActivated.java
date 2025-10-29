package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleActivated(UUID eventId, InstallmentScheduleId aggregateId, Payload payload, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleActivated, ScheduleActivated.Payload> {

    public record Payload() {}

    public ScheduleActivated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static ScheduleActivated of(InstallmentScheduleId scheduleId, Clock clock) {
        return new ScheduleActivated(randomUUID(), scheduleId, new Payload(), clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "ACTIVATED";
    }
}
