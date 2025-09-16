package ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RegulatoryCompliancePolicyEmb {

    @Column(name = "overdue_period_days")
    private Integer overDuePeriodDays;

    @Column(name = "deferral_period_days")
    private Integer deferralPeriodDays;

    @Column(name = "suspicious_period_days")
    private Integer suspiciousPeriodDays;

    public RegulatoryCompliancePolicyEmb() {}

    public Integer getOverDuePeriodDays() {
        return overDuePeriodDays;
    }

    public void setOverDuePeriodDays(Integer overDuePeriodDays) {
        this.overDuePeriodDays = overDuePeriodDays;
    }

    public Integer getDeferralPeriodDays() {
        return deferralPeriodDays;
    }

    public void setDeferralPeriodDays(Integer deferralPeriodDays) {
        this.deferralPeriodDays = deferralPeriodDays;
    }

    public Integer getSuspiciousPeriodDays() {
        return suspiciousPeriodDays;
    }

    public void setSuspiciousPeriodDays(Integer suspiciousPeriodDays) {
        this.suspiciousPeriodDays = suspiciousPeriodDays;
    }
}
