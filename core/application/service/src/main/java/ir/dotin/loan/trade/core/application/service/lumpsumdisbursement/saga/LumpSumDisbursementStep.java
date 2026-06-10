package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.saga;

import ir.dotin.platform.pangaea.saga.api.model.SagaStepId;

public enum LumpSumDisbursementStep implements SagaStepId {
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
