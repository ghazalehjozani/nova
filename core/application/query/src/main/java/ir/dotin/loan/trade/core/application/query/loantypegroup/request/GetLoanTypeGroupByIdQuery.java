package ir.dotin.loan.trade.core.application.query.loantypegroup.request;

import java.util.Set;
import java.util.UUID;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheScope;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheTag;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheableQuery;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeDto;

import lombok.Builder;

@Builder
public record GetLoanTypeGroupByIdQuery(UUID groupId) implements Query<LoanTypeGroupTreeDto>, CacheableQuery {

    @Override
    public Class<LoanTypeGroupTreeDto> getResultType() {
        return LoanTypeGroupTreeDto.class;
    }

    @Override
    public Set<CacheTag> invalidatedBy() {
        return Set.of(
                CacheTag.ofInstance("LoanTypeGroup", groupId),
                CacheTag.ofType("LoanTypeGroup"),
                CacheTag.ofType("TradeLoanType"));
    }

    @Override
    public CacheScope cacheScope() {
        return CacheScope.SHARED;
    }
}
