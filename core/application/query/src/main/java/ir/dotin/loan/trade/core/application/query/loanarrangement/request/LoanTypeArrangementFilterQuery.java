package ir.dotin.loan.trade.core.application.query.loanarrangement.request;

import java.math.BigDecimal;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheScope;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheTag;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheableQuery;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.LoanArrangementQueryResult;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPageRequest;

import lombok.Builder;

@Builder
public record LoanTypeArrangementFilterQuery(
        @Pattern(regexp = "^[0-9]+$", message = "کد نوع تسهیلات فقط می‌تواند شامل اعداد انگلیسی باشد")
        String code,

        String title,
        String currencyType,
        String economicSector,

        @DecimalMin(value = "0.0", inclusive = false, message = "Minimum amount must be positive")
        BigDecimal minAmount,

        @DecimalMin(value = "0.0", inclusive = false, message = "Maximum amount must be positive")
        BigDecimal maxAmount,

        Boolean active,
        Boolean disable,
        DisbursementMethod disbursementMethod,

        @NotNull(message = "Offset page request is required") @Valid
        OffsetPageRequest offsetPageRequest)
        implements Query<LoanArrangementQueryResult>, CacheableQuery {

    public LoanTypeArrangementFilterQuery {
        if (offsetPageRequest == null) {
            offsetPageRequest = OffsetPageRequest.of(0, 20);
        }

        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("Minimum amount cannot be greater than maximum amount");
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

    public static LoanTypeArrangementFilterQuery of(
            @Nullable String code,
            @Nullable String title,
            @Nullable String currencyType,
            @Nullable String economicSector,
            @Nullable BigDecimal minAmount,
            @Nullable BigDecimal maxAmount,
            @Nullable Boolean active,
            @Nullable Boolean disable,
            @Nullable DisbursementMethod disbursementMethod,
            int page,
            int pageSize) {
        return LoanTypeArrangementFilterQuery.builder()
                .code(code)
                .title(title)
                .currencyType(currencyType)
                .economicSector(economicSector)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .active(active)
                .disable(disable)
                .disbursementMethod(disbursementMethod)
                .offsetPageRequest(OffsetPageRequest.of(page, pageSize))
                .build();
    }

    public boolean hasFilters() {
        return (code != null && !code.isBlank())
                || (title != null && !title.isBlank())
                || (currencyType != null && !currencyType.isBlank())
                || (economicSector != null && !economicSector.isBlank())
                || minAmount != null
                || maxAmount != null
                || active != null
                || disable != null
                || disbursementMethod != null;
    }

    public boolean hasAmountRange() {
        return minAmount != null || maxAmount != null;
    }

    public boolean hasCodeFilter() {
        return code != null && !code.isBlank();
    }

    public boolean hasTitleFilter() {
        return title != null && !title.isBlank();
    }

    public boolean hasCurrencyTypeFilter() {
        return currencyType != null && !currencyType.isBlank();
    }

    public boolean hasEconomicSectorFilter() {
        return economicSector != null && !economicSector.isBlank();
    }

    public boolean hasActiveFilter() {
        return active != null;
    }

    public boolean hasDisableFilter() {
        return disable != null;
    }

    public boolean hasDisbursementMethodFilter() {
        return disbursementMethod != null;
    }
}
