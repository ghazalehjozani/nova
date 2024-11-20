package ir.dotin.loan.adapters.persistance.document.base;

import java.math.BigDecimal;

public class MoneyRangeDocument {

    private BigDecimal min;

    private BigDecimal max;

    public MoneyRangeDocument() {
    }

    public MoneyRangeDocument(BigDecimal min, BigDecimal max) {
        this.min = min;
        this.max = max;
    }

    public BigDecimal getMin() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min = min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }

}
