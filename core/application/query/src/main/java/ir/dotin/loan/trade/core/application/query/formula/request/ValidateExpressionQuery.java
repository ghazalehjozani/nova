package ir.dotin.loan.trade.core.application.query.formula.request;

import jakarta.validation.constraints.NotBlank;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.loan.trade.core.application.query.formula.dto.ValidationView;

public record ValidateExpressionQuery(@NotBlank String expression) implements Query<ValidationView> {

    @Override
    public Class<ValidationView> getResultType() {
        return ValidationView.class;
    }
}
