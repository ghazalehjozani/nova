package ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class InstallmentPolicyEmb {
    @Column(name = "installment_period_days")
    private Integer installmentPeriodDays;

    @Column(name = "installment_formula", columnDefinition = "TEXT")
    private String installmentFormula;

    @Column(name = "interest_component_formula", columnDefinition = "TEXT")
    private String interestComponentFormula;

    @Column(name = "installment_payment_type")
    private String installmentPaymentType;

    @Column(name = "define_automatic_installment")
    private Boolean defineAutomaticInstallment;

    public InstallmentPolicyEmb() {}

    public Integer getInstallmentPeriodDays() {
        return installmentPeriodDays;
    }

    public void setInstallmentPeriodDays(Integer installmentPeriodDays) {
        this.installmentPeriodDays = installmentPeriodDays;
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

    public String getInstallmentPaymentType() {
        return installmentPaymentType;
    }

    public void setInstallmentPaymentType(String installmentPaymentType) {
        this.installmentPaymentType = installmentPaymentType;
    }

    public Boolean getDefineAutomaticInstallment() {
        return defineAutomaticInstallment;
    }

    public void setDefineAutomaticInstallment(Boolean defineAutomaticInstallment) {
        this.defineAutomaticInstallment = defineAutomaticInstallment;
    }
}
