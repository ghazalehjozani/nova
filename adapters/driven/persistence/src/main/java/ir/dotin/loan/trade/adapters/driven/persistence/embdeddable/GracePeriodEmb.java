package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class GracePeriodEmb implements Serializable {

    @Nullable
    @Column(name = "grace_period_days")
    private Integer days;

    @Nullable
    @Column(name = "grace_period_months")
    private Integer months;

    @Nullable
    @Column(name = "grace_period_years")
    private Integer years;
}
