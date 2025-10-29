package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleCancelled(UUID eventId, InstallmentScheduleId aggregateId, Payload payload, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleCancelled, ScheduleCancelled.Payload> {

    public record Payload(String reason) {
        public Payload {
            requireNonNull(reason);
        }
    }

    public ScheduleCancelled {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static ScheduleCancelled of(InstallmentScheduleId scheduleId, String reason, Clock clock) {
        return new ScheduleCancelled(randomUUID(), scheduleId, new Payload(reason), clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "CANCELLED";
    }
}
