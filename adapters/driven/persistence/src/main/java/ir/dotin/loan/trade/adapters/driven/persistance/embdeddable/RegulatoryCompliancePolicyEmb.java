package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class RegulatoryCompliancePolicyEmb implements Serializable {

    @Column(name = "overdue_period_days")
    private Integer overDuePeriod;

    @Column(name = "deferral_period_days")
    private Integer deferralPeriod;

    @Column(name = "suspicious_period_days")
    private Integer suspiciousPeriod;
}
