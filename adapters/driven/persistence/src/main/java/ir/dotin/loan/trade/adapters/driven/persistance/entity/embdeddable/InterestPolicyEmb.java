package ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class InterestPolicyEmb {
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

    public InterestPolicyEmb() {}

    public BigDecimal getBaseInterestRate() {
        return baseInterestRate;
    }

    public void setBaseInterestRate(BigDecimal baseInterestRate) {
        this.baseInterestRate = baseInterestRate;
    }

    public BigDecimal getPreferentialMinRate() {
        return preferentialMinRate;
    }

    public void setPreferentialMinRate(BigDecimal preferentialMinRate) {
        this.preferentialMinRate = preferentialMinRate;
    }

    public BigDecimal getPreferentialMaxRate() {
        return preferentialMaxRate;
    }

    public void setPreferentialMaxRate(BigDecimal preferentialMaxRate) {
        this.preferentialMaxRate = preferentialMaxRate;
    }

    public String getInterestFormula() {
        return interestFormula;
    }

    public void setInterestFormula(String interestFormula) {
        this.interestFormula = interestFormula;
    }

    public String getRefundInterestFormula() {
        return refundInterestFormula;
    }

    public void setRefundInterestFormula(String refundInterestFormula) {
        this.refundInterestFormula = refundInterestFormula;
    }

    public Boolean getDailyInterest() {
        return dailyInterest;
    }

    public void setDailyInterest(Boolean dailyInterest) {
        this.dailyInterest = dailyInterest;
    }
}
