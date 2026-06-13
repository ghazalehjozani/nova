package ir.dotin.loan.trade.core.application.service.originateloanfacility.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityOriginationPreparer;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.step.OriginateFacilityData;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.step.OriginateFacilityStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class OriginateLoanFacilityCommandHandler
        implements WorkflowCommandHandler<OriginateLoanFacilityCommand, OriginateFacilityData> {

    @Override
    public Workflow<OriginateFacilityData> definition() {
        // @formatter:off
        return Workflow.singleWrite("originate-loan-facility", writePublishing(originateFacilityStep));
        // @formatter:on
    }

    @Override
    public Result<OriginateFacilityData> seed(OriginateLoanFacilityCommand command) {
        return originationPreparer.prepare(command).map(prepared -> new OriginateFacilityData(command, prepared));
    }

    private final FacilityOriginationPreparer originationPreparer;
    private final OriginateFacilityStep originateFacilityStep;
}
