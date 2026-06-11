package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.workflow;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;

public record CollateralData(
        UUID facilityId,
        UUID requestId,
        List<AddFacilityCollateralCommand.CollateralDto> collaterals,
        long expectedVersion,
        @Nullable List<String> reservedSerials) {

    public static CollateralData initial(
            UUID facilityId,
            UUID requestId,
            List<AddFacilityCollateralCommand.CollateralDto> collaterals,
            long expectedVersion) {
        return new CollateralData(facilityId, requestId, collaterals, expectedVersion, List.of());
    }

    public CollateralData withReservedSerials(List<String> serials) {
        return new CollateralData(facilityId, requestId, collaterals, expectedVersion, serials);
    }
}
