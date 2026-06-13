package ir.dotin.loan.trade.core.application.service.planequalinstallmentschedule.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanEqualInstallmentScheduleCommand;
import ir.dotin.loan.trade.core.application.service.planequalinstallmentschedule.step.PlanScheduleData;
import ir.dotin.loan.trade.core.application.service.planequalinstallmentschedule.step.PlanScheduleStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class PlanEqualInstallmentScheduleCommandHandler
        implements WorkflowCommandHandler<PlanEqualInstallmentScheduleCommand, PlanScheduleData> {

    @Override
    public Workflow<PlanScheduleData> definition() {
        // @formatter:off
        return Workflow.singleWrite("plan-equal-installment-schedule", writePublishing(planScheduleStep));
        // @formatter:on
    }

    @Override
    public Result<PlanScheduleData> seed(PlanEqualInstallmentScheduleCommand command) {
        return Result.success(new PlanScheduleData(command, Unit.INSTANCE));
    }

    private final PlanScheduleStep planScheduleStep;
}
