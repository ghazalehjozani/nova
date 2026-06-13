package ir.dotin.loan.trade.core.application.service.regulardisbursement.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RegularDisbursementCommand;
import ir.dotin.loan.trade.core.application.service.regulardisbursement.step.ApplyRegularDisbursementStep;
import ir.dotin.loan.trade.core.application.service.regulardisbursement.step.RegularDisbursementData;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class RegularDisbursementCommandHandler
        implements WorkflowCommandHandler<RegularDisbursementCommand, RegularDisbursementData> {

    @Override
    public Workflow<RegularDisbursementData> definition() {
        // @formatter:off
        return Workflow.singleWrite("regular-disbursement", writePublishing(applyRegularDisbursementStep));
        // @formatter:on
    }

    @Override
    public Result<RegularDisbursementData> seed(RegularDisbursementCommand command) {
        return Result.success(new RegularDisbursementData(command, Unit.INSTANCE));
    }

    private final ApplyRegularDisbursementStep applyRegularDisbursementStep;
}
