package ir.dotin.loan.trade.core.application.service.defineloantype.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineLoanTypeCommand;
import ir.dotin.loan.trade.core.application.service.defineloantype.component.LoanTypePrerequisitesLoader;
import ir.dotin.loan.trade.core.application.service.defineloantype.step.DefineLoanTypeData;
import ir.dotin.loan.trade.core.application.service.defineloantype.step.DefineLoanTypeStep;

@Service
public final class DefineLoanTypeCommandHandler
        extends WorkflowCommandHandler<DefineLoanTypeCommand, DefineLoanTypeData> {

    @Override
    protected Workflow<DefineLoanTypeData> route(WorkflowRoute<DefineLoanTypeData> route) {
        // @formatter:off
        return route.singleWrite("define-loan-type", defineLoanTypeStep);
        // @formatter:on
    }

    @Override
    protected Result<DefineLoanTypeData> seed(DefineLoanTypeCommand command) {
        return prerequisitesLoader.gather(command).map(prepared -> new DefineLoanTypeData(command, prepared));
    }

    private final LoanTypePrerequisitesLoader prerequisitesLoader;
    private final DefineLoanTypeStep defineLoanTypeStep;

    public DefineLoanTypeCommandHandler(
            WorkflowEngine engine,
            LoanTypePrerequisitesLoader prerequisitesLoader,
            DefineLoanTypeStep defineLoanTypeStep) {
        super(engine);
        this.prerequisitesLoader = prerequisitesLoader;
        this.defineLoanTypeStep = defineLoanTypeStep;
    }
}
