package ir.dotin.loan.trade.core.application.query.loantype.request;

import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import ir.dotin.platform.pangaea.dispatcher.api.cache.CacheDependency;
import ir.dotin.platform.pangaea.dispatcher.api.cache.CacheScope;
import ir.dotin.platform.pangaea.dispatcher.api.query.CacheableQuery;
import ir.dotin.platform.pangaea.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loantype.dto.LoanTypeQueryResult;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPageRequest;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import lombok.Builder;

@Builder
public record LoanTypeFilterQuery(
        @Pattern(regexp = "^[0-9]+$", message = "کد نوع تسهیلات فقط می‌تواند شامل اعداد انگلیسی باشد")
        String code,

        String title,

        @NotNull(message = "Offset page request is required") @Valid
        OffsetPageRequest offsetPageRequest)
        implements Query<LoanTypeQueryResult>, CacheableQuery {

    public LoanTypeFilterQuery {
        if (offsetPageRequest == null) {
            offsetPageRequest = OffsetPageRequest.of(0, 20);
        }
    }

    @AssertTrue(message = "At least one of 'code' or 'title' must be provided")
    public boolean hasFilters() {
        return (code != null && !code.isBlank()) || (title != null && !title.isBlank());
    }

    public boolean hasCodeFilter() {
        return code != null && !code.isBlank();
    }

    public boolean hasTitleFilter() {
        return title != null && !title.isBlank();
    }

    public static LoanTypeFilterQuery of(String code, String title, int page, int pageSize) {
        return LoanTypeFilterQuery.builder()
                .code(code)
                .title(title)
                .offsetPageRequest(OffsetPageRequest.of(page, pageSize))
                .build();
    }

    @Override
    public Class<LoanTypeQueryResult> getResultType() {
        return LoanTypeQueryResult.class;
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
