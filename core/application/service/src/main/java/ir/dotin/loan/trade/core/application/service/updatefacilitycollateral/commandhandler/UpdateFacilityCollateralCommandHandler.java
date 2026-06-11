package ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.step.UpdateCollateralData;
import ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.step.UpdateCollateralStep;

@Service
public final class UpdateFacilityCollateralCommandHandler
        extends WorkflowCommandHandler<UpdateFacilityCollateralCommand, UpdateCollateralData> {

    @Override
    protected Workflow<UpdateCollateralData> route(WorkflowRoute<UpdateCollateralData> route) {
        // @formatter:off
        return route.singleWrite("update-facility-collateral", updateCollateralStep);
        // @formatter:on
    }

    @Override
    protected Result<UpdateCollateralData> seed(UpdateFacilityCollateralCommand command) {
        return Result.success(new UpdateCollateralData(command, Unit.INSTANCE));
    }

    private final UpdateCollateralStep updateCollateralStep;

    public UpdateFacilityCollateralCommandHandler(WorkflowEngine engine, UpdateCollateralStep updateCollateralStep) {
        super(engine);
        this.updateCollateralStep = updateCollateralStep;
    }
}
