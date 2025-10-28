package ir.dotin.loan.trade.core.application.query.shared.pagination;

import java.util.List;

public record OffsetPage<T>(
        List<T> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious) {
    public static <T> OffsetPage<T> empty() {
        return new OffsetPage<>(List.of(), 0, 0, 0, 0, false, false);
    }
}
