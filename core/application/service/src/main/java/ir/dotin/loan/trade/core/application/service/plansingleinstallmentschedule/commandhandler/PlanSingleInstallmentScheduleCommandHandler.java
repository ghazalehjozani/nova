package ir.dotin.loan.trade.core.application.service.plansingleinstallmentschedule.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanSingleInstallmentScheduleCommand;
import ir.dotin.loan.trade.core.application.service.plansingleinstallmentschedule.step.PlanSingleScheduleData;
import ir.dotin.loan.trade.core.application.service.plansingleinstallmentschedule.step.PlanSingleScheduleStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class PlanSingleInstallmentScheduleCommandHandler
        implements WorkflowCommandHandler<PlanSingleInstallmentScheduleCommand, PlanSingleScheduleData> {

    @Override
    public Workflow<PlanSingleScheduleData> definition() {
        // @formatter:off
        return Workflow.singleWrite("plan-single-installment-schedule", writePublishing(planSingleScheduleStep));
        // @formatter:on
    }

    @Override
    public Result<PlanSingleScheduleData> seed(PlanSingleInstallmentScheduleCommand command) {
        return Result.success(new PlanSingleScheduleData(command, Unit.INSTANCE));
    }

    private final PlanSingleScheduleStep planSingleScheduleStep;
}
