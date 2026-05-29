package ir.dotin.loan.trade.adapters.driven.persistence.shared.query;

import java.util.Objects;

import org.springframework.data.domain.Sort;

import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPageRequest;

public final class SortBuilder {

    private SortBuilder() {
        // Utility class - prevent instantiation
    }

    public static Sort buildOffsetSort(OffsetPageRequest pageRequest) {
        if (pageRequest == null) {
            throw new IllegalArgumentException("PageRequest cannot be null");
        }

        Sort.Direction direction = pageRequest.direction() == OffsetPageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, Objects.requireNonNull(pageRequest.sortBy(), "sortBy"))
                .and(Sort.by(Sort.Direction.DESC, "id"));
    }

    public static Sort buildCursorSort() {
        return Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
    }

    public static Sort buildCustomCursorSort(String primarySortField, Sort.Direction primaryDirection) {
        if (primarySortField == null) {
            throw new IllegalArgumentException("Primary sort field cannot be null");
        }
        if (primaryDirection == null) {
            throw new IllegalArgumentException("Sort direction cannot be null");
        }

        return Sort.by(primaryDirection, primarySortField).and(Sort.by(Sort.Direction.DESC, "id"));
    }

    public static Sort buildMultiFieldCursorSort(Sort.Order... sortOrders) {
        if (sortOrders == null || sortOrders.length == 0) {
            throw new IllegalArgumentException("At least one sort order must be provided");
        }

        return Sort.by(sortOrders).and(Sort.by(Sort.Direction.DESC, "id"));
    }

    @FunctionalInterface
    public interface SortStrategy {
        Sort createSort(OffsetPageRequest pageRequest);
    }

    public static Sort buildWithStrategy(OffsetPageRequest pageRequest, SortStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Sort strategy cannot be null");
        }

        return strategy.createSort(pageRequest);
    }
}
