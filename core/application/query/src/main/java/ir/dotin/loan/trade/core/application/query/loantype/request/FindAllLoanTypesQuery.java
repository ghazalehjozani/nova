package ir.dotin.loan.trade.core.application.query.loantype.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loantype.dto.LoanTypeQueryResult;

import lombok.Builder;

@Builder
public record FindAllLoanTypesQuery(
        String cursor,

        @NotNull(message = "Page size is required")
        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer pageSize)
        implements Query<LoanTypeQueryResult> {

    public FindAllLoanTypesQuery {
        if (pageSize == null) {
            pageSize = 20;
        }
    }

    @Override
    public Class<LoanTypeQueryResult> getResultType() {
        return LoanTypeQueryResult.class;
    }

    public static FindAllLoanTypesQuery firstPage(int pageSize) {
        return FindAllLoanTypesQuery.builder().cursor(null).pageSize(pageSize).build();
    }

    public static FindAllLoanTypesQuery withCursor(String cursor, int pageSize) {
        return FindAllLoanTypesQuery.builder().cursor(cursor).pageSize(pageSize).build();
    }

    public boolean isFirstPage() {
        return cursor == null || cursor.isBlank();
    }
}
