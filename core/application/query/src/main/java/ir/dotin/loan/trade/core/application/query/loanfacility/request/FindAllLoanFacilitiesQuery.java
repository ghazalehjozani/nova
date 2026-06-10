package ir.dotin.loan.trade.core.application.query.loanfacility.request;

import java.util.Set;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheTag;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheableQuery;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.LoanFacilityQueryResult;

import lombok.Builder;

@Builder
public record FindAllLoanFacilitiesQuery(
        String cursor,

        @NotNull(message = "Page size is required")
        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer pageSize)
        implements Query<LoanFacilityQueryResult>, CacheableQuery {

    public FindAllLoanFacilitiesQuery {
        if (pageSize == null) {
            pageSize = 20;
        }
    }

    @Override
    public Class<LoanFacilityQueryResult> getResultType() {
        return LoanFacilityQueryResult.class;
    }

    @Override
    public Set<CacheTag> invalidatedBy() {
        return Set.of(CacheTag.ofType("TradeLoanFacility"));
    }

    // Only the hot first page is cached; deeper cursor pages rarely repeat.
    @Override
    public boolean isCacheable() {
        return isFirstPage();
    }

    public static FindAllLoanFacilitiesQuery firstPage(int pageSize) {
        return FindAllLoanFacilitiesQuery.builder()
                .cursor(null)
                .pageSize(pageSize)
                .build();
    }

    public static FindAllLoanFacilitiesQuery withCursor(String cursor, int pageSize) {
        return FindAllLoanFacilitiesQuery.builder()
                .cursor(cursor)
                .pageSize(pageSize)
                .build();
    }

    public boolean isFirstPage() {
        return cursor == null || cursor.isBlank();
    }
}
