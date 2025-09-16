package ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GracePeriodPolicyEmb {
    @Column(name = "min_grace_period_days")
    private Integer minGracePeriodDays;

    @Column(name = "max_grace_period_days")
    private Integer maxGracePeriodDays;

    @Column(name = "grace_period_formula", columnDefinition = "TEXT")
    private String gracePeriodFormula;

    public GracePeriodPolicyEmb() {}

    public Integer getMinGracePeriodDays() {
        return minGracePeriodDays;
    }

    public void setMinGracePeriodDays(Integer minGracePeriodDays) {
        this.minGracePeriodDays = minGracePeriodDays;
    }

    public Integer getMaxGracePeriodDays() {
        return maxGracePeriodDays;
    }

    public void setMaxGracePeriodDays(Integer maxGracePeriodDays) {
        this.maxGracePeriodDays = maxGracePeriodDays;
    }

    public String getGracePeriodFormula() {
        return gracePeriodFormula;
    }

    public void setGracePeriodFormula(String gracePeriodFormula) {
        this.gracePeriodFormula = gracePeriodFormula;
    }
}
