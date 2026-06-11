package ir.dotin.loan.trade.core.application.service.originateloanfacility.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityOriginationPreparer;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.step.OriginateFacilityData;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.step.OriginateFacilityStep;

@Service
public final class OriginateLoanFacilityCommandHandler
        extends WorkflowCommandHandler<OriginateLoanFacilityCommand, OriginateFacilityData> {

    @Override
    protected Workflow<OriginateFacilityData> route(WorkflowRoute<OriginateFacilityData> route) {
        // @formatter:off
        return route.singleWrite("originate-loan-facility", originateFacilityStep);
        // @formatter:on
    }

    @Override
    protected Result<OriginateFacilityData> seed(OriginateLoanFacilityCommand command) {
        return originationPreparer.prepare(command).map(prepared -> new OriginateFacilityData(command, prepared));
    }

    private final FacilityOriginationPreparer originationPreparer;
    private final OriginateFacilityStep originateFacilityStep;

    public OriginateLoanFacilityCommandHandler(
            WorkflowEngine engine,
            FacilityOriginationPreparer originationPreparer,
            OriginateFacilityStep originateFacilityStep) {
        super(engine);
        this.originationPreparer = originationPreparer;
        this.originateFacilityStep = originateFacilityStep;
    }
}
