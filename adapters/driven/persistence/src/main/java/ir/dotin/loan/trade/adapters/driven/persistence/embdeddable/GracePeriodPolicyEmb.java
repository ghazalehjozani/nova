package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import ir.dotin.platform.formula.infrastructure.persistence.embeddable.FormulaIdRefEmb;

import lombok.Data;

@Data
@Embeddable
public class GracePeriodPolicyEmb implements Serializable {
    @Column(name = "min_grace_period_days")
    private Integer minGracePeriodDays;

    @Column(name = "max_grace_period_days")
    private Integer maxGracePeriodDays;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "gracePeriodFormula"))
    private FormulaIdRefEmb gracePeriodFormula;
}
