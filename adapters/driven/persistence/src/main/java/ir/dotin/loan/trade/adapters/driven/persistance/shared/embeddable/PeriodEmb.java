package ir.dotin.loan.trade.adapters.driven.persistance.shared.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PeriodEmb {
    @Column(name = "years")
    private Integer years;

    @Column(name = "months")
    private Integer months;

    @Column(name = "days")
    private Integer days;

    public PeriodEmb() {}

    public Integer getYears() {
        return years;
    }

    public void setYears(Integer years) {
        this.years = years;
    }

    public Integer getMonths() {
        return months;
    }

    public void setMonths(Integer months) {
        this.months = months;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
}
