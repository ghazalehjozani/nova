package ir.dotin.loan.trade.adapters.driven.persistance.shared.embeddable;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AmountEmb {
    @Column(name = "amount_value", precision = 19, scale = 4)
    private BigDecimal value;

    @Column(name = "amount_currency", length = 3)
    private String currency;

    public AmountEmb() {}

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
