package ir.dotin.loan.trade.core.application.query.loanfacility.request;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;

import lombok.Builder;

@Builder
public record GetFacilityByApplicationNumberQuery(
        String applicationNumber, @Nullable String callerBranchCode) implements Query<TradeFacilityQueryDto> {
    @Override
    public Class<TradeFacilityQueryDto> getResultType() {
        return TradeFacilityQueryDto.class;
    }
}
