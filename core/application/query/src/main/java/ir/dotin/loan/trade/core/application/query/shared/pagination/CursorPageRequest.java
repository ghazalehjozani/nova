package ir.dotin.loan.trade.core.application.query.shared.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.jspecify.annotations.Nullable;

public record CursorPageRequest(
        @Nullable String cursor,

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        int pageSize) {
    @SuppressWarnings("ConstantValue")
    public CursorPageRequest {
        if (pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }
    }

    public static CursorPageRequest of(String cursor, int pageSize) {
        return new CursorPageRequest(cursor, pageSize);
    }

    public static CursorPageRequest firstPage(int pageSize) {
        return new CursorPageRequest(null, pageSize);
    }

    public boolean isFirstPage() {
        return cursor == null || cursor.isBlank();
    }
}
