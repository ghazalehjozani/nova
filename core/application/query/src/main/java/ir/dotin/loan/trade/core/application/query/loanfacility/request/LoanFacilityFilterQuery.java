package ir.dotin.loan.trade.core.application.query.loanfacility.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheTag;
import ir.dotin.platform.pangaea.servicelayer.cache.CacheableQuery;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.LoanFacilityQueryResult;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPageRequest;

import lombok.Builder;

@Builder
public record LoanFacilityFilterQuery(
        UUID loanTypeId,
        String customerNumber,

        @PastOrPresent(message = "Create date from cannot be in the future")
        Instant createDateFrom,

        @PastOrPresent(message = "Create date to cannot be in the future")
        Instant createDateTo,

        @DecimalMin(value = "0.0", inclusive = false, message = "Request amount min must be positive")
        BigDecimal requestAmountMin,

        @DecimalMin(value = "0.0", inclusive = false, message = "Request amount max must be positive")
        BigDecimal requestAmountMax,

        FacilityStatus status,

        @NotNull(message = "Offset page request is required") @Valid
        OffsetPageRequest offsetPageRequest)
        implements Query<LoanFacilityQueryResult>, CacheableQuery {

    public LoanFacilityFilterQuery {
        if (offsetPageRequest == null) {
            offsetPageRequest = OffsetPageRequest.of(0, 20);
        }

        if (createDateFrom != null && createDateTo != null && createDateFrom.isAfter(createDateTo)) {
            throw new IllegalArgumentException("Create date from cannot be after create date to");
        }

        if (requestAmountMin != null && requestAmountMax != null && requestAmountMin.compareTo(requestAmountMax) > 0) {
            throw new IllegalArgumentException("Request amount min cannot be greater than request amount max");
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

    public static LoanFacilityFilterQuery of(
            @Nullable UUID loanTypeId,
            @Nullable String customerNumber,
            @Nullable Instant createDateFrom,
            @Nullable Instant createDateTo,
            @Nullable BigDecimal requestAmountMin,
            @Nullable BigDecimal requestAmountMax,
            @Nullable FacilityStatus status,
            int page,
            int pageSize) {
        return LoanFacilityFilterQuery.builder()
                .loanTypeId(loanTypeId)
                .customerNumber(customerNumber)
                .createDateFrom(createDateFrom)
                .createDateTo(createDateTo)
                .requestAmountMin(requestAmountMin)
                .requestAmountMax(requestAmountMax)
                .status(status)
                .offsetPageRequest(OffsetPageRequest.of(page, pageSize))
                .build();
    }

    public boolean hasFilters() {
        return loanTypeId != null
                || customerNumber != null && !customerNumber.isBlank()
                || createDateFrom != null
                || createDateTo != null
                || requestAmountMin != null
                || requestAmountMax != null
                || status != null;
    }

    public boolean hasDateRange() {
        return createDateFrom != null || createDateTo != null;
    }

    public boolean hasAmountRange() {
        return requestAmountMin != null || requestAmountMax != null;
    }
}
