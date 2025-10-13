package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class GracePeriodEmb implements Serializable {

    @Column(name = "grace_period_days")
    private Integer days;

    @Column(name = "grace_period_months")
    private Integer months;

    @Column(name = "grace_period_years")
    private Integer years;
}
