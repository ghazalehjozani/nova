package ir.dotin.loan.trade.core.application.service.formula.workflow;

import ir.dotin.platform.pangaea.workflow.api.model.WorkflowStepId;

public enum UpdateFormulaStep implements WorkflowStepId {
    VALIDATE_IN_FCB("validate-in-fcb"),
    PERSIST("persist");

    private final String id;

    UpdateFormulaStep(String id) {
        this.id = id;
    }

    @Override
    public String value() {
        return id;
    }
}
