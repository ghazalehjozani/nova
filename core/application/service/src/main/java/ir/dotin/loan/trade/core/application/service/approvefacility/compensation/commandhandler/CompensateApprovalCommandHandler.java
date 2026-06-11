package ir.dotin.loan.trade.core.application.service.approvefacility.compensation.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalCommand;
import ir.dotin.loan.trade.core.application.service.approvefacility.compensation.step.RevertApprovalStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public final class CompensateApprovalCommandHandler
        extends WorkflowCommandHandler<CompensateApprovalCommand, CompensateApprovalCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        // @formatter:off
        return route.singleWrite("compensate-approval", revertApprovalStep);
        // @formatter:on
    }

    @Override
    protected Result<Data> seed(CompensateApprovalCommand command) {
        log.warn("Compensating approval for facility: {}", command.loanFacilityId());
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CompensateApprovalCommand command, Unit prepared) {}

    private final RevertApprovalStep revertApprovalStep;

    CompensateApprovalCommandHandler(WorkflowEngine engine, RevertApprovalStep revertApprovalStep) {
        super(engine);
        this.revertApprovalStep = revertApprovalStep;
    }
}
