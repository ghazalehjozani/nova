package ir.dotin.loan.trade.core.application.query.loanfacility.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.LoanFacilityQueryResult;

import lombok.Builder;

@Builder
public record FindAllLoanFacilitiesQuery(
        String cursor,
        @NotNull(message = "Page size is required")
                @Min(value = 1, message = "Page size must be at least 1")
                @Max(value = 100, message = "Page size cannot exceed 100")
                Integer pageSize)
        implements Query<LoanFacilityQueryResult> {

    public FindAllLoanFacilitiesQuery {
        if (pageSize == null) {
            pageSize = 20;
        }
    }

    @Override
    public Class<LoanFacilityQueryResult> getResultType() {
        return LoanFacilityQueryResult.class;
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
