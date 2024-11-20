package ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments;

public class RepaymentPriorityPolicyDocument {

   private Integer installmentMainAmountPriority;

   private Integer installmentInterestAmountPriority;

   private Integer installmentPenaltyAmountPriority;

   private Integer installmentIncomeAmountPriority;

   private Integer insuranceAmountPriority;

   private Integer insurancePenaltyAmountPriority;

   private boolean hasEqualPriority;

    public RepaymentPriorityPolicyDocument() {
    }

    public RepaymentPriorityPolicyDocument(Integer installmentMainAmountPriority,
                                           Integer installmentInterestAmountPriority,
                                           Integer installmentPenaltyAmountPriority,
                                           Integer installmentIncomeAmountPriority,
                                           Integer insuranceAmountPriority,
                                           Integer insurancePenaltyAmountPriority,
                                           boolean hasEqualPriority) {
        this.installmentMainAmountPriority = installmentMainAmountPriority;
        this.installmentInterestAmountPriority = installmentInterestAmountPriority;
        this.installmentPenaltyAmountPriority = installmentPenaltyAmountPriority;
        this.installmentIncomeAmountPriority = installmentIncomeAmountPriority;
        this.insuranceAmountPriority = insuranceAmountPriority;
        this.insurancePenaltyAmountPriority = insurancePenaltyAmountPriority;
        this.hasEqualPriority = hasEqualPriority;
    }

    public Integer getInstallmentMainAmountPriority() {
        return installmentMainAmountPriority;
    }

    public void setInstallmentMainAmountPriority(Integer installmentMainAmountPriority) {
        this.installmentMainAmountPriority = installmentMainAmountPriority;
    }

    public Integer getInstallmentInterestAmountPriority() {
        return installmentInterestAmountPriority;
    }

    public void setInstallmentInterestAmountPriority(Integer installmentInterestAmountPriority) {
        this.installmentInterestAmountPriority = installmentInterestAmountPriority;
    }

    public Integer getInstallmentPenaltyAmountPriority() {
        return installmentPenaltyAmountPriority;
    }

    public void setInstallmentPenaltyAmountPriority(Integer installmentPenaltyAmountPriority) {
        this.installmentPenaltyAmountPriority = installmentPenaltyAmountPriority;
    }

    public Integer getInstallmentIncomeAmountPriority() {
        return installmentIncomeAmountPriority;
    }

    public void setInstallmentIncomeAmountPriority(Integer installmentIncomeAmountPriority) {
        this.installmentIncomeAmountPriority = installmentIncomeAmountPriority;
    }

    public Integer getInsuranceAmountPriority() {
        return insuranceAmountPriority;
    }

    public void setInsuranceAmountPriority(Integer insuranceAmountPriority) {
        this.insuranceAmountPriority = insuranceAmountPriority;
    }

    public Integer getInsurancePenaltyAmountPriority() {
        return insurancePenaltyAmountPriority;
    }

    public void setInsurancePenaltyAmountPriority(Integer insurancePenaltyAmountPriority) {
        this.insurancePenaltyAmountPriority = insurancePenaltyAmountPriority;
    }

    public boolean isHasEqualPriority() {
        return hasEqualPriority;
    }

    public void setHasEqualPriority(boolean hasEqualPriority) {
        this.hasEqualPriority = hasEqualPriority;
    }

}
