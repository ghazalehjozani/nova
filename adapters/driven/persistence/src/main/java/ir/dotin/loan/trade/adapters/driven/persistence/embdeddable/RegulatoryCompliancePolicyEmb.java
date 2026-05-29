package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class RegulatoryCompliancePolicyEmb implements Serializable {

    @Nullable
    @Column(name = "overdue_period_days")
    private Integer overDuePeriod;

    @Nullable
    @Column(name = "deferral_period_days")
    private Integer deferralPeriod;

    @Nullable
    @Column(name = "suspicious_period_days")
    private Integer suspiciousPeriod;
}
