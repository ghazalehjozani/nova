package ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityPaidOffCommand;
import ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.step.ClosePaidOffFacilityStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class CloseFacilityPaidOffCommandHandler
        implements WorkflowCommandHandler<CloseFacilityPaidOffCommand, CloseFacilityPaidOffCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("close-facility-paid-off", writePublishing(closePaidOffFacilityStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(CloseFacilityPaidOffCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CloseFacilityPaidOffCommand command, Unit prepared) {}

    private final ClosePaidOffFacilityStep closePaidOffFacilityStep;
}
