package ir.dotin.loan.trade.core.application.query.loanarrangement.request;

import java.util.Set;
import java.util.UUID;

import ir.dotin.platform.pangaea.dispatcher.api.cache.CacheDependency;
import ir.dotin.platform.pangaea.dispatcher.api.cache.CacheScope;
import ir.dotin.platform.pangaea.dispatcher.api.query.CacheableQuery;
import ir.dotin.platform.pangaea.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import lombok.Builder;

@Builder
public record GetLoanArrangementByCodeQuery(UUID uid, String code)
        implements Query<TradeLoanArrangementQueryDto>, CacheableQuery {
    @Override
    public Class<TradeLoanArrangementQueryDto> getResultType() {
        return TradeLoanArrangementQueryDto.class;
    }

    @Override
    public Set<CacheDependency> invalidatedBy() {
        return Set.of(CacheDependency.ofType(TradeLoanArrangement.class));
    }

    @Override
    public CacheScope cacheScope() {
        return CacheScope.SHARED;
    }
}
