package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class GracePeriodPolicyEmb implements Serializable {
    @Column(name = "min_grace_period_days")
    private Integer minGracePeriodDays;

    @Column(name = "max_grace_period_days")
    private Integer maxGracePeriodDays;

    @Column(name = "grace_period_formula", columnDefinition = "TEXT")
    private String gracePeriodFormula;
}
