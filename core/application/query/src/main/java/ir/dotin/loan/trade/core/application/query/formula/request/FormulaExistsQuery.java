package ir.dotin.loan.trade.core.application.query.formula.request;

import jakarta.validation.constraints.NotBlank;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.loan.trade.core.application.query.formula.dto.FormulaExistsView;

public record FormulaExistsQuery(@NotBlank String code) implements Query<FormulaExistsView> {

    @Override
    public Class<FormulaExistsView> getResultType() {
        return FormulaExistsView.class;
    }
}
