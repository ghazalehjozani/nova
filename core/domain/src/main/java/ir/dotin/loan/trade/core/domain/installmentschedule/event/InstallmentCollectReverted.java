package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record InstallmentCollectReverted(
        UUID eventId, UUID aggregateId, String eventType, UUID loanFacilityId, Instant createdAt)
        implements InstallmentScheduleEvents<InstallmentCollectReverted> {

    public InstallmentCollectReverted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(loanFacilityId);
        requireNonNull(createdAt);
    }

    public static InstallmentCollectReverted of(
            @NonNull InstallmentScheduleId scheduleId, @NonNull LoanFacilityId loanFacilityId, Instant occurredAt) {

        return new InstallmentCollectReverted(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.INSTALLMENT_COLLECT_REVERTED.getFullType(),
                loanFacilityId.value(),
                occurredAt);
    }
}
