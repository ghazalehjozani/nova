package ir.dotin.loan.trade.core.application.service.regulardisbursement.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RegularDisbursementCommand;
import ir.dotin.loan.trade.core.application.service.regulardisbursement.step.ApplyRegularDisbursementStep;
import ir.dotin.loan.trade.core.application.service.regulardisbursement.step.RegularDisbursementData;

@Service
public final class RegularDisbursementCommandHandler
        extends WorkflowCommandHandler<RegularDisbursementCommand, RegularDisbursementData> {

    @Override
    protected Workflow<RegularDisbursementData> route(WorkflowRoute<RegularDisbursementData> route) {
        // @formatter:off
        return route.singleWrite("regular-disbursement", applyRegularDisbursementStep);
        // @formatter:on
    }

    @Override
    protected Result<RegularDisbursementData> seed(RegularDisbursementCommand command) {
        return Result.success(new RegularDisbursementData(command, Unit.INSTANCE));
    }

    private final ApplyRegularDisbursementStep applyRegularDisbursementStep;

    public RegularDisbursementCommandHandler(
            WorkflowEngine engine, ApplyRegularDisbursementStep applyRegularDisbursementStep) {
        super(engine);
        this.applyRegularDisbursementStep = applyRegularDisbursementStep;
    }
}
