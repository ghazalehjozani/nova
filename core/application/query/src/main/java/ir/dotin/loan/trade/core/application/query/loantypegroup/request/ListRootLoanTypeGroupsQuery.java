package ir.dotin.loan.trade.core.application.query.loantypegroup.request;

import java.util.Set;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheScope;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheTag;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheableQuery;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeListResult;

import lombok.Builder;

@Builder
public record ListRootLoanTypeGroupsQuery() implements Query<LoanTypeGroupTreeListResult>, CacheableQuery {

    @Override
    public Class<LoanTypeGroupTreeListResult> getResultType() {
        return LoanTypeGroupTreeListResult.class;
    }

    @Override
    public Set<CacheTag> invalidatedBy() {
        return Set.of(CacheTag.ofType("LoanTypeGroup"), CacheTag.ofType("TradeLoanType"));
    }

    @Override
    public CacheScope cacheScope() {
        return CacheScope.SHARED;
    }
}
