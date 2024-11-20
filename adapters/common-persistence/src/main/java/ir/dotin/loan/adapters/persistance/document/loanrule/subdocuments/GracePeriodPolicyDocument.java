package ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments;

import ir.dotin.loan.adapters.persistance.document.base.DurationRangeDocument;

public class GracePeriodPolicyDocument {

    private DurationRangeDocument gracePeriodRange;

    private String formula;

    public GracePeriodPolicyDocument() {
    }

    public GracePeriodPolicyDocument(DurationRangeDocument gracePeriodRange, String formula) {
        this.gracePeriodRange = gracePeriodRange;
        this.formula = formula;
    }

    public DurationRangeDocument getGracePeriodRange() {
        return gracePeriodRange;
    }

    public void setGracePeriodRange(
            DurationRangeDocument gracePeriodRange) {
        this.gracePeriodRange = gracePeriodRange;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

}
