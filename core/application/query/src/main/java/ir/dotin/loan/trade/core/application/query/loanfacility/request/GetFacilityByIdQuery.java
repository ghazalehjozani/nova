package ir.dotin.loan.trade.core.application.query.loanfacility.request;

import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheTag;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheableQuery;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;

import lombok.Builder;

@Builder
public record GetFacilityByIdQuery(
        UUID loanFacilityId, @Nullable String callerBranchCode)
        implements Query<TradeFacilityQueryDto>, CacheableQuery {
    @Override
    public Class<TradeFacilityQueryDto> getResultType() {
        return TradeFacilityQueryDto.class;
    }

    @Override
    public Set<CacheTag> invalidatedBy() {
        return Set.of(CacheTag.ofInstance("TradeLoanFacility", loanFacilityId));
    }
}
