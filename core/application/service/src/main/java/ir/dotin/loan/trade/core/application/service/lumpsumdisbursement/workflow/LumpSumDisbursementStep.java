package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow;

import ir.dotin.platform.pangaea.workflow.api.model.WorkflowStepId;

public enum LumpSumDisbursementStep implements WorkflowStepId {
    VALIDATE_FACILITY("validate-facility"),
    RESOLVE_ACCOUNTS("resolve-accounts"),
    POST_TRANSACTIONS("post-transactions"),
    APPLY_DISBURSEMENT("apply-disbursement");

    private final String id;

    LumpSumDisbursementStep(String id) {
        this.id = id;
    }

    @Override
    public String value() {
        return id;
    }
}
