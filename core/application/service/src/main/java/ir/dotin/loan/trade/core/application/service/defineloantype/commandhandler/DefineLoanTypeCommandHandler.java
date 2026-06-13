package ir.dotin.loan.trade.core.application.service.defineloantype.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineLoanTypeCommand;
import ir.dotin.loan.trade.core.application.service.defineloantype.component.LoanTypePrerequisitesLoader;
import ir.dotin.loan.trade.core.application.service.defineloantype.step.DefineLoanTypeData;
import ir.dotin.loan.trade.core.application.service.defineloantype.step.DefineLoanTypeStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class DefineLoanTypeCommandHandler
        implements WorkflowCommandHandler<DefineLoanTypeCommand, DefineLoanTypeData> {

    @Override
    public Workflow<DefineLoanTypeData> definition() {
        // @formatter:off
        return Workflow.singleWrite("define-loan-type", writePublishing(defineLoanTypeStep));
        // @formatter:on
    }

    @Override
    public Result<DefineLoanTypeData> seed(DefineLoanTypeCommand command) {
        return prerequisitesLoader.gather(command).map(prepared -> new DefineLoanTypeData(command, prepared));
    }

    private final LoanTypePrerequisitesLoader prerequisitesLoader;
    private final DefineLoanTypeStep defineLoanTypeStep;
}
