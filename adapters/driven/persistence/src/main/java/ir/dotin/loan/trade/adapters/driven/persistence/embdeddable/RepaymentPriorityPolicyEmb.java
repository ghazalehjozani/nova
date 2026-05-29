package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class RepaymentPriorityPolicyEmb implements Serializable {

    @Nullable
    @Column(name = "installment_main_amount_priority")
    private Integer installmentMainAmountPriority;

    @Nullable
    @Column(name = "installment_interest_amount_priority")
    private Integer installmentInterestAmountPriority;

    @Nullable
    @Column(name = "installment_penalty_amount_priority")
    private Integer installmentPenaltyAmountPriority;

    @Nullable
    @Column(name = "installment_income_amount_priority")
    private Integer installmentIncomeAmountPriority;

    @Nullable
    @Column(name = "insurance_amount_priority")
    private Integer insuranceAmountPriority;

    @Nullable
    @Column(name = "insurance_penalty_amount_priority")
    private Integer insurancePenaltyAmountPriority;

    @Nullable
    @Column(name = "has_equal_priority")
    private Boolean hasEqualPriority;
}
