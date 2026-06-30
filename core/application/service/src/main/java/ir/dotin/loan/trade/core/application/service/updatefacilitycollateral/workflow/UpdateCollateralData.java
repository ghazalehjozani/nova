package ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.workflow;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateCollateralCommand;

public record UpdateCollateralData(
        UUID facilityId,
        UUID requestId,
        List<UpdateCollateralCommand.CollateralDto> collaterals,
        long expectedVersion,
        @Nullable List<String> reservedSerials) {

    public static UpdateCollateralData initial(
            UUID facilityId,
            UUID requestId,
            List<UpdateCollateralCommand.CollateralDto> collaterals,
            long expectedVersion) {
        return new UpdateCollateralData(facilityId, requestId, collaterals, expectedVersion, List.of());
    }

    public UpdateCollateralData withReservedSerials(List<String> serials) {
        return new UpdateCollateralData(facilityId, requestId, collaterals, expectedVersion, serials);
    }
}
