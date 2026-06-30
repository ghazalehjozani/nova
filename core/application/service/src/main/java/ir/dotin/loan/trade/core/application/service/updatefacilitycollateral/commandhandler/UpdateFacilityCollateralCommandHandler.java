package ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.commandhandler;

import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateCollateralCommand;
import ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.component.UpdateCollateralDependencyLoader;
import ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.mapper.UpdateCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.step.UpdateCollateralStep;
import ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.workflow.UpdateCollateralData;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class UpdateFacilityCollateralCommandHandler
        implements WorkflowCommandHandler<UpdateCollateralCommand, UpdateCollateralData> {

    @Override
    public Workflow<UpdateCollateralData> definition() {
        // @formatter:off
        return Workflow.singleWrite("update-facility-collateral", writePublishing(updateCollateralStep));
        // @formatter:on
    }

    @Override
    public Result<UpdateCollateralData> seed(UpdateCollateralCommand command) {
        List<Collateral> collaterals = mapper.toCollaterals(command.collaterals());
        Result<Unit> validation =
                dependencyLoader.validateAndCheck(LoanFacilityId.of(command.loanFacilityId()), collaterals);
        if (validation.isFailure()) {
            return Result.failure(validation.err().orElseThrow());
        }
        return Result.success(UpdateCollateralData.initial(
                command.loanFacilityId(), command.uid(), command.collaterals(), command.version()));
    }

    private final UpdateCollateralCommandMapper mapper;
    private final UpdateCollateralDependencyLoader dependencyLoader;
    private final UpdateCollateralStep updateCollateralStep;
}
