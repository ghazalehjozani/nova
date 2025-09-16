package ir.dotin.loan.trade.adapters.driven.persistance.shared.embeddable;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class InterestRateEmb {
    @Column(name = "rate_value", precision = 5, scale = 4)
    private BigDecimal value;

    @Column(name = "rate_type", length = 20)
    private String type;

    public InterestRateEmb() {}

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
