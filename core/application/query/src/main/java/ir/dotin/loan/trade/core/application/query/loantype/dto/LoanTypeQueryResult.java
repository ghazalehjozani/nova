package ir.dotin.loan.trade.core.application.query.loantype.dto;

import java.util.List;

import ir.dotin.platform.pangaea.dispatcher.api.query.QueryResult;

import lombok.Builder;

@Builder
public record LoanTypeQueryResult(
        List<TradeLoanTypeQueryDto> loanTypes,
        String nextCursor,
        String previousCursor,
        boolean hasNext,
        boolean hasPrevious,
        Integer currentPage,
        Integer pageSize,
        Long totalElements,
        Integer totalPages)
        implements QueryResult {

    public static LoanTypeQueryResult forCursor(
            List<TradeLoanTypeQueryDto> loanTypes,
            String nextCursor,
            String previousCursor,
            boolean hasNext,
            boolean hasPrevious) {
        return LoanTypeQueryResult.builder()
                .loanTypes(loanTypes)
                .nextCursor(nextCursor)
                .previousCursor(previousCursor)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }

    public static LoanTypeQueryResult forOffset(
            List<TradeLoanTypeQueryDto> loanTypes,
            int currentPage,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious) {
        return LoanTypeQueryResult.builder()
                .loanTypes(loanTypes)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }
}
