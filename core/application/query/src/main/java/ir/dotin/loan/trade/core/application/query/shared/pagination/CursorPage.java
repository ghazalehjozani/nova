package ir.dotin.loan.trade.core.application.query.shared.pagination;

import java.util.List;

import org.jspecify.annotations.Nullable;

public record CursorPage<T>(
        List<T> content,
        @Nullable String nextCursor,
        @Nullable String previousCursor,
        boolean hasNext,
        boolean hasPrevious) {
    public static <T> CursorPage<T> empty() {
        return new CursorPage<>(List.of(), null, null, false, false);
    }
}
