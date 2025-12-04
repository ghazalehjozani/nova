package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record RestructuringReverted(UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements InstallmentScheduleEvents<RestructuringReverted> {

    public RestructuringReverted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static RestructuringReverted of(InstallmentScheduleId scheduleId, Clock clock) {
        return new RestructuringReverted(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.RESTRUCTURING_REVERTED.getFullType(),
                clock.instant());
    }
}
