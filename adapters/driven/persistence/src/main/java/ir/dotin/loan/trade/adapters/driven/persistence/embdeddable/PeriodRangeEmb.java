package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import ir.dotin.platform.adapter.persistence.embeddable.PeriodEmb;

import lombok.Data;

@Data
@Embeddable
public class PeriodRangeEmb {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "years", column = @Column(name = "min_years")),
        @AttributeOverride(name = "months", column = @Column(name = "min_months")),
        @AttributeOverride(name = "days", column = @Column(name = "min_days"))
    })
    private PeriodEmb minPeriod;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "years", column = @Column(name = "max_years")),
        @AttributeOverride(name = "months", column = @Column(name = "max_months")),
        @AttributeOverride(name = "days", column = @Column(name = "max_days"))
    })
    private PeriodEmb maxPeriod;
}
