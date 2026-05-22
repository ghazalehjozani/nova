package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.saga;

import java.util.List;
import java.util.UUID;

import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;

/**
 * Saga input for the add-facility-collateral use case.
 *
 * <p>Carries only serializable primitives ({@code facilityId}, the command {@code uid} used as the reservation request
 * id, and the original {@code CollateralDto} list). The aggregate is re-loaded from the repository inside each step,
 * never stashed here — see {@link AddFacilityCollateralSaga} (instance-safe, multi-instance topology).
 */
public record AddFacilityCollateralInput(
        UUID facilityId, UUID requestId, List<AddFacilityCollateralCommand.CollateralDto> collaterals)
        implements SagaInput {

    public static AddFacilityCollateralInput of(
            UUID facilityId, UUID requestId, List<AddFacilityCollateralCommand.CollateralDto> collaterals) {
        return new AddFacilityCollateralInput(facilityId, requestId, collaterals);
    }
}
