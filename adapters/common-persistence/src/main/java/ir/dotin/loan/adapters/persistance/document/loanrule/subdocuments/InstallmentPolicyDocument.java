package ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments;

import ir.dotin.loan.adapters.persistance.document.base.DurationRangeDocument;

public class InstallmentPolicyDocument {

    private DurationRangeDocument durationRange;

    private String installmentFormula;

    private String interestComponentFormula;

    private String paymentType;

    private boolean isDefineAutomaticInstallment;

    public InstallmentPolicyDocument() {
    }

    public InstallmentPolicyDocument(DurationRangeDocument durationRange, String installmentFormula,
                                     String interestComponentFormula, String paymentType,
                                     boolean isDefineAutomaticInstallment) {
        this.durationRange = durationRange;
        this.installmentFormula = installmentFormula;
        this.interestComponentFormula = interestComponentFormula;
        this.paymentType = paymentType;
        this.isDefineAutomaticInstallment = isDefineAutomaticInstallment;
    }

    public DurationRangeDocument getDurationRange() {
        return durationRange;
    }

    public void setDurationRange(
            DurationRangeDocument durationRange) {
        this.durationRange = durationRange;
    }

    public String getInstallmentFormula() {
        return installmentFormula;
    }

    public void setInstallmentFormula(String installmentFormula) {
        this.installmentFormula = installmentFormula;
    }

    public String getInterestComponentFormula() {
        return interestComponentFormula;
    }

    public void setInterestComponentFormula(String interestComponentFormula) {
        this.interestComponentFormula = interestComponentFormula;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public boolean isDefineAutomaticInstallment() {
        return isDefineAutomaticInstallment;
    }

    public void setDefineAutomaticInstallment(boolean defineAutomaticInstallment) {
        isDefineAutomaticInstallment = defineAutomaticInstallment;
    }

}
