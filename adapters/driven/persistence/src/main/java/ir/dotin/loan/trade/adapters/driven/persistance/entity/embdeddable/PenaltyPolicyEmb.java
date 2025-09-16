package ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PenaltyPolicyEmb {

    @Column(name = "penalty_rate", precision = 10, scale = 6)
    private BigDecimal penaltyRate;

    @Column(name = "deferral_interest_rate", precision = 10, scale = 6)
    private BigDecimal deferralInterestRate;

    @Column(name = "penalty_formula", columnDefinition = "TEXT")
    private String penaltyFormula;

    @Column(name = "penalty_payment_type")
    private String penaltyPaymentType;

    public PenaltyPolicyEmb() {}

    public BigDecimal getPenaltyRate() {
        return penaltyRate;
    }

    public void setPenaltyRate(BigDecimal penaltyRate) {
        this.penaltyRate = penaltyRate;
    }

    public BigDecimal getDeferralInterestRate() {
        return deferralInterestRate;
    }

    public void setDeferralInterestRate(BigDecimal deferralInterestRate) {
        this.deferralInterestRate = deferralInterestRate;
    }

    public String getPenaltyFormula() {
        return penaltyFormula;
    }

    public void setPenaltyFormula(String penaltyFormula) {
        this.penaltyFormula = penaltyFormula;
    }

    public String getPenaltyPaymentType() {
        return penaltyPaymentType;
    }

    public void setPenaltyPaymentType(String penaltyPaymentType) {
        this.penaltyPaymentType = penaltyPaymentType;
    }
}
