package ir.dotin.loan.trade.core.application.query.loanfacility.dto;

import java.util.List;

import ir.dotin.platform.dispatcher.api.query.QueryResult;

import lombok.Builder;

@Builder
public record LoanFacilityQueryResult(
        List<TradeFacilityQueryDto> facilities,
        String nextCursor,
        String previousCursor,
        boolean hasNext,
        boolean hasPrevious,
        Integer currentPage,
        Integer pageSize,
        Long totalElements,
        Integer totalPages)
        implements QueryResult {

    public static LoanFacilityQueryResult forCursor(
            List<TradeFacilityQueryDto> facilities,
            String nextCursor,
            String previousCursor,
            boolean hasNext,
            boolean hasPrevious) {
        return LoanFacilityQueryResult.builder()
                .facilities(facilities)
                .nextCursor(nextCursor)
                .previousCursor(previousCursor)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }

    public static LoanFacilityQueryResult forOffset(
            List<TradeFacilityQueryDto> facilities,
            int currentPage,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious) {
        return LoanFacilityQueryResult.builder()
                .facilities(facilities)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }
}
