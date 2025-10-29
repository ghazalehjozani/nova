package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

public sealed interface InstallmentScheduleEvents<T extends Record & InstallmentScheduleEvents<T, P>, P>
        extends DomainEvent<T, P>
        permits EqualInstallmentsCreated,
        GradualInstallmentsCreated,
                ScheduleRestructured,
                ScheduleCancelled,
                ScheduleActivated,
                ScheduleOnHold,
                ScheduleCompleted,
                ScheduleStateTransitioned {

    String EVENT_TYPE_PREFIX = "INSTALLMENT_SCHEDULE_";

    @Override
    @NonNull
    InstallmentScheduleId aggregateId();

    @Override
    default Class<InstallmentSchedule> aggregateType() {
        return InstallmentSchedule.class;
    }
}
