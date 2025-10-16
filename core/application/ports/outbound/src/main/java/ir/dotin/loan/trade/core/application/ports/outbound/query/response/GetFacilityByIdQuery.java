package ir.dotin.loan.trade.core.application.ports.outbound.query.response;

import java.util.UUID;

import ir.dotin.platform.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeFacilityQueryDto;

import lombok.Builder;

@Builder
public record GetFacilityByIdQuery(UUID uid, UUID loanFacilityId) implements Query<TradeFacilityQueryDto> {
    @Override
    public Class<TradeFacilityQueryDto> getResultType() {
        return TradeFacilityQueryDto.class;
    }
}
