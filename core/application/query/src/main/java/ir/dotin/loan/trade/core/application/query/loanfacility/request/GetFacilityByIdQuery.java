package ir.dotin.loan.trade.core.application.query.loanfacility.request;

import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.dispatcher.api.cache.CacheDependency;
import ir.dotin.platform.pangaea.dispatcher.api.query.CacheableQuery;
import ir.dotin.platform.pangaea.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

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
    public Set<CacheDependency> invalidatedBy() {
        return Set.of(CacheDependency.of(TradeLoanFacility.class, loanFacilityId));
    }
}
