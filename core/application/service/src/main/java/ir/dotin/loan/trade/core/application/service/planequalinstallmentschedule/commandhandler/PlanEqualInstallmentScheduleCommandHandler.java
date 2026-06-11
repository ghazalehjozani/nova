package ir.dotin.loan.trade.core.application.service.planequalinstallmentschedule.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanEqualInstallmentScheduleCommand;
import ir.dotin.loan.trade.core.application.service.planequalinstallmentschedule.step.PlanScheduleData;
import ir.dotin.loan.trade.core.application.service.planequalinstallmentschedule.step.PlanScheduleStep;

@Service
public final class PlanEqualInstallmentScheduleCommandHandler
        extends WorkflowCommandHandler<PlanEqualInstallmentScheduleCommand, PlanScheduleData> {

    @Override
    protected Workflow<PlanScheduleData> route(WorkflowRoute<PlanScheduleData> route) {
        // @formatter:off
        return route.singleWrite("plan-equal-installment-schedule", planScheduleStep);
        // @formatter:on
    }

    @Override
    protected Result<PlanScheduleData> seed(PlanEqualInstallmentScheduleCommand command) {
        return Result.success(new PlanScheduleData(command, Unit.INSTANCE));
    }

    private final PlanScheduleStep planScheduleStep;

    public PlanEqualInstallmentScheduleCommandHandler(WorkflowEngine engine, PlanScheduleStep planScheduleStep) {
        super(engine);
        this.planScheduleStep = planScheduleStep;
    }
}
