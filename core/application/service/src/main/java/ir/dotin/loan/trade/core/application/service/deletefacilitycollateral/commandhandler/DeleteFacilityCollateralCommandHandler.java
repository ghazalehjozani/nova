package ir.dotin.loan.trade.core.application.service.deletefacilitycollateral.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DeleteFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.service.deletefacilitycollateral.component.DeleteFacilityCollateralDependencyLoader;
import ir.dotin.loan.trade.core.application.service.deletefacilitycollateral.step.DeleteCollateralStep;
import ir.dotin.loan.trade.core.application.service.deletefacilitycollateral.workflow.DeleteCollateralData;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class DeleteFacilityCollateralCommandHandler
        implements WorkflowCommandHandler<DeleteFacilityCollateralCommand, DeleteCollateralData> {

    @Override
    public Workflow<DeleteCollateralData> definition() {
        // @formatter:off
        return Workflow.singleWrite("delete-facility-collateral", writePublishing(deleteCollateralStep));
        // @formatter:on
    }

    @Override
    public Result<DeleteCollateralData> seed(DeleteFacilityCollateralCommand command) {
        var validation = dependencyLoader.validateAndCheck(
                LoanFacilityId.of(command.loanFacilityId()), command.collateralSerials());
        if (validation.isFailure()) {
            return Result.failure(validation.err().orElseThrow());
        }
        return Result.success(
                DeleteCollateralData.initial(command.loanFacilityId(), command.collateralSerials(), command.version()));
    }

    private final DeleteFacilityCollateralDependencyLoader dependencyLoader;
    private final DeleteCollateralStep deleteCollateralStep;
}
