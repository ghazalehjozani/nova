package ir.dotin.loan.trade.core.application.query.loantype.request;

import java.util.Set;
import java.util.UUID;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheScope;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheTag;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheableQuery;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;

import lombok.Builder;

@Builder
public record GetLoanTypeByIdQuery(UUID loanTypeId) implements Query<TradeLoanTypeQueryDto>, CacheableQuery {
    @Override
    public Class<TradeLoanTypeQueryDto> getResultType() {
        return TradeLoanTypeQueryDto.class;
    }

    @Override
    public Set<CacheTag> invalidatedBy() {
        return Set.of(CacheTag.ofInstance("TradeLoanType", loanTypeId));
    }

    @Override
    public CacheScope cacheScope() {
        return CacheScope.SHARED;
    }
}
