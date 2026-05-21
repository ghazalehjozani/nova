package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.saga;

import ir.dotin.platform.saga.api.model.SagaStepId;

public enum AddFacilityCollateralStep implements SagaStepId {
    RESERVE_COLLATERALS("reserve-collaterals"),
    ADD_COLLATERAL("add-collateral");

    private final String id;

    AddFacilityCollateralStep(String id) {
        this.id = id;
    }

    @Override
    public String value() {
        return id;
    }
}
