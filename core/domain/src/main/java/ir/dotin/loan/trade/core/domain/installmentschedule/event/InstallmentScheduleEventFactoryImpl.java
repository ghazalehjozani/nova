package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.domain.annotation.DomainFactory;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.event.InstallmentScheduleEventFactory;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentId;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentPaymentRecord;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

@DomainFactory
public class InstallmentScheduleEventFactoryImpl implements InstallmentScheduleEventFactory {

    @Override
    public DomainEvent<?> createEqualInstallmentsCreatedEvent(
            @NonNull InstallmentScheduleId scheduleId,
            @NonNull LoanFacilityId loanFacilityId,
            @NonNull BigDecimal totalAmount,
            int installmentCount,
            @NonNull Instant createdAt) {
        return EqualInstallmentsCreated.of(
                scheduleId,
                loanFacilityId,
                totalAmount,
                installmentCount,
                Clock.fixed(createdAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createUnequalInstallmentsCreatedEvent(
            @NonNull InstallmentScheduleId scheduleId,
            @NonNull InstallmentScheduleStatus installmentScheduleStatus,
            @NonNull LoanFacilityId loanFacilityId,
            @NonNull BigDecimal totalAmount,
            @NonNull List<InstallmentId> specifications,
            @NonNull Instant createdAt) {
        return GradualInstallmentsCreated.of(
                scheduleId,
                installmentScheduleStatus,
                loanFacilityId,
                totalAmount,
                specifications,
                Clock.fixed(createdAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createScheduleRestructuredEvent(
            @NonNull InstallmentScheduleId scheduleId,
            @NonNull String reason,
            List<InstallmentScheduleId> installmentScheduleHistories,
            @NonNull Instant occurredAt) {
        return ScheduleRestructured.of(
                scheduleId, reason, installmentScheduleHistories, Clock.fixed(occurredAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createScheduleCancelledEvent(
            @NonNull InstallmentScheduleId scheduleId, @NonNull String reason, @NonNull Instant occurredAt) {
        return ScheduleCancelled.of(scheduleId, reason, Clock.fixed(occurredAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createScheduleActivatedEvent(
            @NonNull InstallmentScheduleId scheduleId,
            List<InstallmentScheduleId> installmentScheduleHistories,
            @NonNull Instant occurredAt) {
        return ScheduleActivated.of(
                scheduleId, installmentScheduleHistories, Clock.fixed(occurredAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createScheduleOnHoldEvent(
            @NonNull InstallmentScheduleId scheduleId, @NonNull String reason, @NonNull Instant occurredAt) {
        return ScheduleOnHold.of(scheduleId, reason, Clock.fixed(occurredAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createScheduleCompletedEvent(
            @NonNull InstallmentScheduleId scheduleId, @NonNull Instant occurredAt) {
        return ScheduleCompleted.of(scheduleId, Clock.fixed(occurredAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createStateTransitionEvent(
            @NonNull InstallmentScheduleId scheduleId,
            @NonNull InstallmentScheduleStatus fromStatus,
            @NonNull InstallmentScheduleStatus toStatus,
            @Nullable String reason,
            @NonNull Instant occurredAt) {
        return ScheduleStateTransitioned.of(
                scheduleId, fromStatus, toStatus, reason, Clock.fixed(occurredAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createScheduleCreationRevertedEvent(
            @NonNull InstallmentScheduleId scheduleId, @Nullable String reason, @NonNull Instant occurredAt) {
        return ScheduleCreationReverted.of(scheduleId, reason, Clock.fixed(occurredAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createActivationRevertedEvent(
            @NonNull InstallmentScheduleId scheduleId, @NonNull Instant occurredAt) {
        return ActivationReverted.of(scheduleId, Clock.fixed(occurredAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createRestructuringRevertedEvent(
            @NonNull InstallmentScheduleId scheduleId, @NonNull Instant occurredAt) {
        return RestructuringReverted.of(scheduleId, Clock.fixed(occurredAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createInstallmentCreationRevertedEvent(
            @NonNull InstallmentScheduleId scheduleId,
            @NonNull List<InstallmentId> removedInstallmentIds,
            @NonNull Instant occurredAt) {
        return InstallmentCreationReverted.of(
                scheduleId, removedInstallmentIds, Clock.fixed(occurredAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createCompletionRevertedEvent(
            @NonNull InstallmentScheduleId scheduleId, @NonNull Instant occurredAt) {
        return CompletionReverted.of(scheduleId, Clock.fixed(occurredAt, ZoneId.systemDefault()));
    }

    @Override
    public DomainEvent<?> createInstallmentCollectedEvent(
            @NonNull InstallmentScheduleId scheduleId,
            @NonNull LoanFacilityId loanFacilityId,
            @NonNull InstallmentPaymentRecord paymentRecord,
            @NonNull InstallmentStatus installmentStatus,
            @NonNull Instant occurredAt) {

        return InstallmentCollected.of(scheduleId, loanFacilityId, paymentRecord, installmentStatus, occurredAt);
    }
}
