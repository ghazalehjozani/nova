package ir.dotin.loan.trade.core.application.query.loanfacility.request;

import java.util.UUID;

import ir.dotin.platform.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;

import lombok.Builder;

@Builder
public record GetFacilityByIdQuery(UUID loanFacilityId) implements Query<TradeFacilityQueryDto> {
    @Override
    public Class<TradeFacilityQueryDto> getResultType() {
        return TradeFacilityQueryDto.class;
    }
}
