package ir.dotin.loan.trade.core.application.service.approvefacility.compensation.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalCommand;
import ir.dotin.loan.trade.core.application.service.approvefacility.compensation.step.RevertApprovalStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Slf4j
@Service
@RequiredArgsConstructor
public final class CompensateApprovalCommandHandler
        implements WorkflowCommandHandler<CompensateApprovalCommand, CompensateApprovalCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("compensate-approval", writePublishing(revertApprovalStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(CompensateApprovalCommand command) {
        log.warn("Compensating approval for facility: {}", command.loanFacilityId());
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CompensateApprovalCommand command, Unit prepared) {}

    private final RevertApprovalStep revertApprovalStep;
}
