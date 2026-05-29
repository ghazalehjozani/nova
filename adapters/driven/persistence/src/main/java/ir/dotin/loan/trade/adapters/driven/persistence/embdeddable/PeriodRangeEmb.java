package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.persistence.jpa.embeddable.PeriodEmb;

import lombok.Data;

@Data
@Embeddable
public class PeriodRangeEmb {

    @Nullable
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "years", column = @Column(name = "min_years")),
        @AttributeOverride(name = "months", column = @Column(name = "min_months")),
        @AttributeOverride(name = "days", column = @Column(name = "min_days"))
    })
    private PeriodEmb minPeriod;

    @Nullable
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "years", column = @Column(name = "max_years")),
        @AttributeOverride(name = "months", column = @Column(name = "max_months")),
        @AttributeOverride(name = "days", column = @Column(name = "max_days"))
    })
    private PeriodEmb maxPeriod;
}
