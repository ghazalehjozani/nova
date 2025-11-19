package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga;

import ir.dotin.platform.saga.api.model.SagaStepId;

public enum IssueFacilityContractStep implements SagaStepId {
    VALIDATE_FACILITY("validate-facility"),
    PREPARE_TRANSACTION("prepare-transaction"),
    POST_TRANSACTION("post-transaction"),
    UPDATE_FACILITY_STATE("update-facility-state");

    private final String id;

    IssueFacilityContractStep(String id) {
        this.id = id;
    }

    @Override
    public String value() {
        return id;
    }
}
