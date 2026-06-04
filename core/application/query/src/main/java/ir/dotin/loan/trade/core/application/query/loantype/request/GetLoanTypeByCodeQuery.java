package ir.dotin.loan.trade.core.application.query.loantype.request;

import java.util.Set;

import ir.dotin.platform.pangaea.dispatcher.api.cache.CacheDependency;
import ir.dotin.platform.pangaea.dispatcher.api.cache.CacheScope;
import ir.dotin.platform.pangaea.dispatcher.api.query.CacheableQuery;
import ir.dotin.platform.pangaea.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import lombok.Builder;

@Builder
public record GetLoanTypeByCodeQuery(String code) implements Query<TradeLoanTypeQueryDto>, CacheableQuery {
    @Override
    public Class<TradeLoanTypeQueryDto> getResultType() {
        return TradeLoanTypeQueryDto.class;
    }

    @Override
    public Set<CacheDependency> invalidatedBy() {
        return Set.of(CacheDependency.ofType(TradeLoanType.class));
    }

    @Override
    public CacheScope cacheScope() {
        return CacheScope.SHARED;
    }
}
