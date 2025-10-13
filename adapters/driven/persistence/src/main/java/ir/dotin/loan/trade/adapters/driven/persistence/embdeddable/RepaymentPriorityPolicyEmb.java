package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class RepaymentPriorityPolicyEmb implements Serializable {

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
}
