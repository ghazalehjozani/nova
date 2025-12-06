package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.saga;

import ir.dotin.platform.saga.api.model.SagaStepId;

public enum FullLoanFacilityLifecycleStep implements SagaStepId {
    VALIDATE_INPUT("validate-input"),
    ORIGINATE_FACILITY("originate-facility"),
    SUBMIT_FOR_APPROVAL("submit-for-approval"),
    APPROVE_FACILITY("approve-facility"),
    ISSUE_CONTRACT("issue-contract"),
    EXECUTE_DISBURSEMENT("execute-disbursement");

    private final String id;

    FullLoanFacilityLifecycleStep(String id) {
        this.id = id;
    }

    @Override
    public String value() {
        return id;
    }
}
