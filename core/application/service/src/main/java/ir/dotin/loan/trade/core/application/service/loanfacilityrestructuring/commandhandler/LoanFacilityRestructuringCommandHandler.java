package ir.dotin.loan.trade.core.application.service.loanfacilityrestructuring.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LoanFacilityRestructuringCommand;
import ir.dotin.loan.trade.core.application.service.loanfacilityrestructuring.step.RestructureFacilityStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class LoanFacilityRestructuringCommandHandler
        implements WorkflowCommandHandler<
                LoanFacilityRestructuringCommand, LoanFacilityRestructuringCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("loan-facility-restructuring", writePublishing(restructureFacilityStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(LoanFacilityRestructuringCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(LoanFacilityRestructuringCommand command, Unit prepared) {}

    private final RestructureFacilityStep restructureFacilityStep;
}
