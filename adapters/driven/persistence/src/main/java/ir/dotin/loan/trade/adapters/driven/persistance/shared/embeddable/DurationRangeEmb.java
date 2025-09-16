package ir.dotin.loan.trade.adapters.driven.persistance.shared.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DurationRangeEmb {
    @Column(name = "min_duration_days")
    private Long minDurationDays;

    @Column(name = "max_duration_days")
    private Long maxDurationDays;

    public DurationRangeEmb() {}

    public Long getMinDurationDays() {
        return minDurationDays;
    }

    public void setMinDurationDays(Long minDurationDays) {
        this.minDurationDays = minDurationDays;
    }

    public Long getMaxDurationDays() {
        return maxDurationDays;
    }

    public void setMaxDurationDays(Long maxDurationDays) {
        this.maxDurationDays = maxDurationDays;
    }
}
