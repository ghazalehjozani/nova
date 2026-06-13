package ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.compensation.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCloseFacilityPaidOffCommand;
import ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.compensation.step.RevertClosePaidOffStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class CompensateCloseFacilityPaidOffCommandHandler
        implements WorkflowCommandHandler<
                CompensateCloseFacilityPaidOffCommand, CompensateCloseFacilityPaidOffCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("compensate-close-facility-paid-off", writePublishing(revertClosePaidOffStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(CompensateCloseFacilityPaidOffCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CompensateCloseFacilityPaidOffCommand command, Unit prepared) {}

    private final RevertClosePaidOffStep revertClosePaidOffStep;
}
