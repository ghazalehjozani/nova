package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.workflow;

import ir.dotin.platform.pangaea.workflow.api.model.WorkflowStepId;

public enum AddFacilityCollateralStep implements WorkflowStepId {
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
