package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ScheduleOnHold(
        UUID eventId, UUID aggregateId, String eventName, String eventType, Payload payload, Instant createdAt)
        implements InstallmentScheduleEvents<ScheduleOnHold, ScheduleOnHold.Payload> {

    public record Payload(String reason) {
        public Payload {
            requireNonNull(reason);
        }
    }

    public ScheduleOnHold {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventName);
        requireNonNull(eventType);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static ScheduleOnHold of(InstallmentScheduleId scheduleId, String reason, Clock clock) {
        return new ScheduleOnHold(
                randomUUID(),
                scheduleId.value(),
                ScheduleOnHold.class.getSimpleName(),
                InstallmentScheduleEventType.ON_HOLD.getFullType(),
                new Payload(reason),
                clock.instant());
    }
}
