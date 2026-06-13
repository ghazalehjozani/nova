package ir.dotin.loan.trade.core.application.service.cancelfacility.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CancelFacilityCommand;
import ir.dotin.loan.trade.core.application.service.cancelfacility.step.CancelFacilityStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class CancelFacilityCommandHandler
        implements WorkflowCommandHandler<CancelFacilityCommand, CancelFacilityCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("cancel-facility", writePublishing(cancelFacilityStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(CancelFacilityCommand command) {
        return Result.success(new Data(command, Unit.INSTANCE));
    }

    public record Data(CancelFacilityCommand command, Unit prepared) {}

    private final CancelFacilityStep cancelFacilityStep;
}
