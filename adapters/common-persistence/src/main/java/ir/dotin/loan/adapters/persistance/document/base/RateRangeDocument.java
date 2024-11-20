package ir.dotin.loan.adapters.persistance.document.base;

public class RateRangeDocument {

    private Double min;

    private Double max;

    public RateRangeDocument() {
    }

    public RateRangeDocument(Double min, Double max) {
        this.min = min;
        this.max = max;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }
}
