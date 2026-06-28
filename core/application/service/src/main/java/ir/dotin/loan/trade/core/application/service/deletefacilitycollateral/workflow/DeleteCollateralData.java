package ir.dotin.loan.trade.core.application.service.deletefacilitycollateral.workflow;

import java.util.List;
import java.util.UUID;

public record DeleteCollateralData(UUID facilityId, List<String> collateralSerials, long expectedVersion) {

    public static DeleteCollateralData initial(UUID facilityId, List<String> collateralSerials, long expectedVersion) {
        return new DeleteCollateralData(facilityId, collateralSerials, expectedVersion);
    }
}
