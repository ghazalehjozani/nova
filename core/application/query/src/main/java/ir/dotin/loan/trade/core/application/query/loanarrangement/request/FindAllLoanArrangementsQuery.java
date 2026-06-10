package ir.dotin.loan.trade.core.application.query.loanarrangement.request;

import java.util.Set;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheScope;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheTag;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheableQuery;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.LoanArrangementQueryResult;

import lombok.Builder;

@Builder
public record FindAllLoanArrangementsQuery(
        String cursor,

        @NotNull(message = "Page size is required")
        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer pageSize)
        implements Query<LoanArrangementQueryResult>, CacheableQuery {

    public FindAllLoanArrangementsQuery {
        if (pageSize == null) {
            pageSize = 20;
        }
    }

    @Override
    public Class<LoanArrangementQueryResult> getResultType() {
        return LoanArrangementQueryResult.class;
    }

    @Override
    public Set<CacheTag> invalidatedBy() {
        return Set.of(CacheTag.ofType("TradeLoanArrangement"));
    }

    @Override
    public CacheScope cacheScope() {
        return CacheScope.SHARED;
    }

    // Only the hot first page is cached; deeper cursor pages rarely repeat.
    @Override
    public boolean isCacheable() {
        return isFirstPage();
    }

    public static FindAllLoanArrangementsQuery firstPage(int pageSize) {
        return FindAllLoanArrangementsQuery.builder()
                .cursor(null)
                .pageSize(pageSize)
                .build();
    }

    public static FindAllLoanArrangementsQuery withCursor(String cursor, int pageSize) {
        return FindAllLoanArrangementsQuery.builder()
                .cursor(cursor)
                .pageSize(pageSize)
                .build();
    }

    public boolean isFirstPage() {
        return cursor == null || cursor.isBlank();
    }
}
