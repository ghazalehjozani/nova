package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import ir.dotin.platform.formula.infrastructure.persistence.embeddable.FormulaIdRefEmb;

import lombok.Data;

@Data
@Embeddable
public class InterestPolicyEmb implements Serializable {
    @Column(name = "base_interest_rate", precision = 10, scale = 6)
    private BigDecimal baseInterestRate;

    @Column(name = "preferential_min_rate", precision = 10, scale = 6)
    private BigDecimal preferentialMinRate;

    @Column(name = "preferential_max_rate", precision = 10, scale = 6)
    private BigDecimal preferentialMaxRate;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "interest_formula"))
    private FormulaIdRefEmb interestFormula;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "refund_interest_formula"))
    private FormulaIdRefEmb refundInterestFormula;

    @Column(name = "daily_interest")
    private Boolean dailyInterest;
}
