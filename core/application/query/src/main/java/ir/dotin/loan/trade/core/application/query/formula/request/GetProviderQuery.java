package ir.dotin.loan.trade.core.application.query.formula.request;

import jakarta.validation.constraints.NotBlank;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.loan.trade.core.application.query.formula.dto.ProviderView;

public record GetProviderQuery(@NotBlank String code) implements Query<ProviderView> {

    @Override
    public Class<ProviderView> getResultType() {
        return ProviderView.class;
    }
}
