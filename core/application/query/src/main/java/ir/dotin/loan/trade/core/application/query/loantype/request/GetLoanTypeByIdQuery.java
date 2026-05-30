package ir.dotin.loan.trade.core.application.query.loantype.request;

import java.util.Set;
import java.util.UUID;

import ir.dotin.platform.pangaea.dispatcher.api.cache.CacheDependency;
import ir.dotin.platform.pangaea.dispatcher.api.cache.CacheScope;
import ir.dotin.platform.pangaea.dispatcher.api.query.CacheableQuery;
import ir.dotin.platform.pangaea.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import lombok.Builder;

@Builder
public record GetLoanTypeByIdQuery(UUID loanTypeId) implements Query<TradeLoanTypeQueryDto>, CacheableQuery {
    @Override
    public Class<TradeLoanTypeQueryDto> getResultType() {
        return TradeLoanTypeQueryDto.class;
    }

    @Override
    public Set<CacheDependency> invalidatedBy() {
        return Set.of(CacheDependency.of(TradeLoanType.class, loanTypeId));
    }

    @Override
    public CacheScope cacheScope() {
        return CacheScope.SHARED;
    }
}
