package ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RepaymentPriorityPolicyEmb {

    @Column(name = "installment_main_amount_priority")
    private Integer installmentMainAmountPriority;

    @Column(name = "installment_interest_amount_priority")
    private Integer installmentInterestAmountPriority;

    @Column(name = "installment_penalty_amount_priority")
    private Integer installmentPenaltyAmountPriority;

    @Column(name = "installment_income_amount_priority")
    private Integer installmentIncomeAmountPriority;

    @Column(name = "insurance_amount_priority")
    private Integer insuranceAmountPriority;

    @Column(name = "insurance_penalty_amount_priority")
    private Integer insurancePenaltyAmountPriority;

    @Column(name = "has_equal_priority")
    private Boolean hasEqualPriority;

    public RepaymentPriorityPolicyEmb() {}

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

    public Boolean getHasEqualPriority() {
        return hasEqualPriority;
    }

    public void setHasEqualPriority(Boolean hasEqualPriority) {
        this.hasEqualPriority = hasEqualPriority;
    }
}
