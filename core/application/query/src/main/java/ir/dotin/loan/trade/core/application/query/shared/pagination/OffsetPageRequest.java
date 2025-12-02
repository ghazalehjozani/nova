package ir.dotin.loan.trade.core.application.query.shared.pagination;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OffsetPageRequest(
        @NotNull(message = "Page number is required") @Min(value = 0, message = "Page number cannot be negative")
        Integer page,

        @NotNull(message = "Page size is required")
        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer pageSize,

        @Nullable String sortBy,
        @Nullable SortDirection direction) {
    public OffsetPageRequest {
        if (page == null) {
            page = 0;
        }
        if (pageSize == null) {
            pageSize = 20;
        }
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createdAt";
        }
        if (direction == null) {
            direction = SortDirection.DESC;
        }
    }

    public static OffsetPageRequest of(int page, int pageSize) {
        return new OffsetPageRequest(page, pageSize, "createdAt", SortDirection.DESC);
    }

    public static OffsetPageRequest of(int page, int pageSize, String sortBy, SortDirection direction) {
        return new OffsetPageRequest(page, pageSize, sortBy, direction);
    }

    public static OffsetPageRequest firstPage(int pageSize) {
        return new OffsetPageRequest(0, pageSize, "createdAt", SortDirection.DESC);
    }

    public boolean isFirstPage() {
        return page == 0;
    }

    public enum SortDirection {
        ASC,
        DESC
    }
}
