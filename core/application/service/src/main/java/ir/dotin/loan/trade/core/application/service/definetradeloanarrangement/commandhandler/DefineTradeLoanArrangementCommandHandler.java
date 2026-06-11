package ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.component.ArrangementPreparer;
import ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.step.DefineArrangementData;
import ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.step.DefineArrangementStep;

@Service
public final class DefineTradeLoanArrangementCommandHandler
        extends WorkflowCommandHandler<DefineTradeLoanArrangementCommand, DefineArrangementData> {

    @Override
    protected Workflow<DefineArrangementData> route(WorkflowRoute<DefineArrangementData> route) {
        // @formatter:off
        return route.singleWrite("define-trade-loan-arrangement", defineArrangementStep);
        // @formatter:on
    }

    @Override
    protected Result<DefineArrangementData> seed(DefineTradeLoanArrangementCommand command) {
        return arrangementPreparer.prepare(command).map(prepared -> new DefineArrangementData(command, prepared));
    }

    private final ArrangementPreparer arrangementPreparer;
    private final DefineArrangementStep defineArrangementStep;

    public DefineTradeLoanArrangementCommandHandler(
            WorkflowEngine engine,
            ArrangementPreparer arrangementPreparer,
            DefineArrangementStep defineArrangementStep) {
        super(engine);
        this.arrangementPreparer = arrangementPreparer;
        this.defineArrangementStep = defineArrangementStep;
    }
}
