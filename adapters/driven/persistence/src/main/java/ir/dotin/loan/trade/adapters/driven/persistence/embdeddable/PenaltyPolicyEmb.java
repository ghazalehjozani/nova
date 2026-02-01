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
public class PenaltyPolicyEmb implements Serializable {

    @Column(name = "penalty_rate", precision = 10, scale = 6)
    private BigDecimal penaltyRate;

    @Column(name = "deferral_interest_rate", precision = 10, scale = 6)
    private BigDecimal deferralInterestRate;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "penalty_formula"))
    private FormulaIdRefEmb penaltyFormula;

    @Column(name = "penalty_payment_type")
    private String penaltyPaymentType;
}
