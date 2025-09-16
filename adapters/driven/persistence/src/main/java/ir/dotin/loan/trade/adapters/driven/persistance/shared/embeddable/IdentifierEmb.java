package ir.dotin.loan.trade.adapters.driven.persistance.shared.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class IdentifierEmb {
    @Column(name = "id_value", unique = true, nullable = false)
    private String value;

    @Column(name = "id_type", length = 10)
    private String type;

    public IdentifierEmb() {}

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
