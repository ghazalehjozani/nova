package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

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

    @Column(name = "interest_formula", columnDefinition = "TEXT")
    private String interestFormula;

    @Column(name = "refund_interest_formula", columnDefinition = "TEXT")
    private String refundInterestFormula;

    @Column(name = "daily_interest")
    private Boolean dailyInterest;
}
