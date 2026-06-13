package ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.step.UpdateCollateralData;
import ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.step.UpdateCollateralStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class UpdateFacilityCollateralCommandHandler
        implements WorkflowCommandHandler<UpdateFacilityCollateralCommand, UpdateCollateralData> {

    @Override
    public Workflow<UpdateCollateralData> definition() {
        // @formatter:off
        return Workflow.singleWrite("update-facility-collateral", writePublishing(updateCollateralStep));
        // @formatter:on
    }

    @Override
    public Result<UpdateCollateralData> seed(UpdateFacilityCollateralCommand command) {
        return Result.success(new UpdateCollateralData(command, Unit.INSTANCE));
    }

    private final UpdateCollateralStep updateCollateralStep;
}
