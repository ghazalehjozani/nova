package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.formula.infrastructure.persistence.embeddable.FormulaIdRefEmb;

import lombok.Data;

@Data
@Embeddable
public class InstallmentPolicyEmb implements Serializable {

    @Nullable
    @Column(name = "installment_period_days")
    private Integer installmentPeriodDays;

    @Nullable
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "installment_formula"))
    private FormulaIdRefEmb installmentFormula;

    @Nullable
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "interest_component_formula"))
    private FormulaIdRefEmb interestComponentFormula;

    @Nullable
    @Column(name = "installment_payment_type")
    private String installmentPaymentType;
}
