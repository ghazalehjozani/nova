package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class PenaltyPolicyEmb implements Serializable {

    @Column(name = "penalty_rate", precision = 10, scale = 6)
    private BigDecimal penaltyRate;

    @Column(name = "deferral_interest_rate", precision = 10, scale = 6)
    private BigDecimal deferralInterestRate;

    @Column(name = "penalty_formula", columnDefinition = "TEXT")
    private String penaltyFormula;

    @Column(name = "penalty_payment_type")
    private String penaltyPaymentType;
}
