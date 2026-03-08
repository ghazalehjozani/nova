package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;

public sealed interface InstallmentScheduleEvents<T extends Record & InstallmentScheduleEvents<T>>
        extends DomainEvent<T>
        permits ActivationReverted,
                CompletionReverted,
                EqualInstallmentsCreated,
                GradualInstallmentsCreated,
                InstallmentCollectReverted,
                InstallmentCollected,
                InstallmentCreationReverted,
                RestructuringReverted,
                ScheduleActivated,
                ScheduleCancelled,
                ScheduleCompleted,
                ScheduleCreationReverted,
                ScheduleOnHold,
                ScheduleRestructured,
                ScheduleStateTransitioned {

    @Override
    default Class<InstallmentSchedule> aggregateType() {
        return InstallmentSchedule.class;
    }
}
