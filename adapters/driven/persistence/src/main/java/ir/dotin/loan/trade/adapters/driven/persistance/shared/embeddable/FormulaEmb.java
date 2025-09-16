package ir.dotin.loan.trade.adapters.driven.persistance.shared.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class FormulaEmb {
    @Column(name = "formula", columnDefinition = "TEXT")
    private String formula;

    @Column(name = "formula_description", length = 500)
    private String description;

    public FormulaEmb() {}

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
