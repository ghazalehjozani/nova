package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class InstallmentPolicyEmb implements Serializable {
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
}
