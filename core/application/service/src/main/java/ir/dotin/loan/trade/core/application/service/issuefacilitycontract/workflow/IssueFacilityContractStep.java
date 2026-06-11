package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow;

import ir.dotin.platform.pangaea.workflow.api.model.WorkflowStepId;

public enum IssueFacilityContractStep implements WorkflowStepId {
    VALIDATE_FACILITY("validate-facility"),
    OPEN_ACCOUNTS("open-accounts"),
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
