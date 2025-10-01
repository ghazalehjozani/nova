package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.domain.entity.Identity;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.event.InstallmentScheduleEventFactory;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;

// TODO: change IDs with concrete ID
public class InstallmentScheduleEventFactoryImpl implements InstallmentScheduleEventFactory {
    @Override
    public DomainEvent<?, ?> createEqualInstallmentsCreatedEvent(
            @NonNull Identity scheduleId,
            @NonNull Identity loanFacilityId,
            @NonNull Money totalAmount,
            int installmentCount,
            @NonNull Instant createdAt) {
        return null;
    }

    @Override
    public DomainEvent<?, ?> createUnequalInstallmentsCreatedEvent(
            @NonNull Identity scheduleId,
            @NonNull Identity loanFacilityId,
            @NonNull Money totalAmount,
            @NonNull List<InstallmentSpec> specifications,
            @NonNull Instant createdAt) {
        return null;
    }

    @Override
    public DomainEvent<?, ?> createScheduleRestructuredEvent(
            @NonNull Identity scheduleId,
            @NonNull String reason,
            @NonNull Map<String, Object> changes,
            @NonNull Instant occurredAt) {
        return null;
    }

    @Override
    public DomainEvent<?, ?> createScheduleCancelledEvent(
            @NonNull Identity scheduleId, @NonNull String reason, @NonNull Instant occurredAt) {
        return null;
    }

    @Override
    public DomainEvent<?, ?> createScheduleActivatedEvent(@NonNull Identity scheduleId, @NonNull Instant occurredAt) {
        return null;
    }

    @Override
    public DomainEvent<?, ?> createScheduleOnHoldEvent(
            @NonNull Identity scheduleId, @NonNull String reason, @NonNull Instant occurredAt) {
        return null;
    }

    @Override
    public DomainEvent<?, ?> createScheduleCompletedEvent(@NonNull Identity scheduleId, @NonNull Instant occurredAt) {
        return null;
    }

    @Override
    public DomainEvent<?, ?> createStateTransitionEvent(
            @NonNull Identity scheduleId,
            @NonNull InstallmentScheduleStatus fromStatus,
            @NonNull InstallmentScheduleStatus toStatus,
            @Nullable String reason,
            @NonNull Instant occurredAt) {
        return null;
    }
}
