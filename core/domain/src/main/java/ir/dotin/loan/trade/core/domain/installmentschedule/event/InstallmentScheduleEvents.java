package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;

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

    @Override
    default Class<InstallmentSchedule> aggregateType() {
        return InstallmentSchedule.class;
    }
}
