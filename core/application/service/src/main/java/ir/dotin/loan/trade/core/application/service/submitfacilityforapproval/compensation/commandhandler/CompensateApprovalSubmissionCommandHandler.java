package ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.compensation.commandhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalSubmissionCommand;
import ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.compensation.step.RevertApprovalSubmissionStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class CompensateApprovalSubmissionCommandHandler
        implements WorkflowCommandHandler<
                CompensateApprovalSubmissionCommand, CompensateApprovalSubmissionCommandHandler.Data> {

    private static final Logger log = LoggerFactory.getLogger(CompensateApprovalSubmissionCommandHandler.class);

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("compensate-approval-submission", writePublishing(revertApprovalSubmissionStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(CompensateApprovalSubmissionCommand command) {
        log.warn("Compensating approval submission for facility: {}", command.loanFacilityId());
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CompensateApprovalSubmissionCommand command, Unit prepared) {}

    private final RevertApprovalSubmissionStep revertApprovalSubmissionStep;
}
