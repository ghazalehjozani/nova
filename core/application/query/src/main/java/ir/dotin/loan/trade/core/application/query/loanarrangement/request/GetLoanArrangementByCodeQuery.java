package ir.dotin.loan.trade.core.application.query.loanarrangement.request;

import java.util.Set;
import java.util.UUID;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheScope;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheTag;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheableQuery;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;

import lombok.Builder;

@Builder
public record GetLoanArrangementByCodeQuery(UUID uid, String code)
        implements Query<TradeLoanArrangementQueryDto>, CacheableQuery {
    @Override
    public Class<TradeLoanArrangementQueryDto> getResultType() {
        return TradeLoanArrangementQueryDto.class;
    }

    @Override
    public Set<CacheTag> invalidatedBy() {
        return Set.of(CacheTag.ofType("TradeLoanArrangement"));
    }

    @Override
    public CacheScope cacheScope() {
        return CacheScope.SHARED;
    }
}
