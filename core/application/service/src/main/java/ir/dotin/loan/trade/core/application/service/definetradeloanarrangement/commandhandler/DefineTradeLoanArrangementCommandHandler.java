package ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.component.ArrangementPreparer;
import ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.step.DefineArrangementData;
import ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.step.DefineArrangementStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class DefineTradeLoanArrangementCommandHandler
        implements WorkflowCommandHandler<DefineTradeLoanArrangementCommand, DefineArrangementData> {

    @Override
    public Workflow<DefineArrangementData> definition() {
        // @formatter:off
        return Workflow.singleWrite("define-trade-loan-arrangement", writePublishing(defineArrangementStep));
        // @formatter:on
    }

    @Override
    public Result<DefineArrangementData> seed(DefineTradeLoanArrangementCommand command) {
        return arrangementPreparer.prepare(command).map(prepared -> new DefineArrangementData(command, prepared));
    }

    private final ArrangementPreparer arrangementPreparer;
    private final DefineArrangementStep defineArrangementStep;
}
