package ir.dotin.loan.trade.adapters.driving.rest.shared.pagination;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import ir.dotin.platform.adapter.rest.response.CursorPaginationInfo;
import ir.dotin.platform.adapter.rest.response.PagedResponse;
import ir.dotin.platform.dispatcher.api.context.StandardHeaders;
import ir.dotin.platform.dispatcher.api.query.QueryResult;

public class CursorPaginationHelper {

    public static <T extends QueryResult> ResponseEntity<PagedResponse<T>> createPaginatedResponse(T result) {
        try {
            String nextCursor =
                    (String) result.getClass().getMethod("nextCursor").invoke(result);
            String previousCursor =
                    (String) result.getClass().getMethod("previousCursor").invoke(result);
            Boolean hasNext = (Boolean) result.getClass().getMethod("hasNext").invoke(result);
            Boolean hasPrevious =
                    (Boolean) result.getClass().getMethod("hasPrevious").invoke(result);

            return createPaginatedResponseWithValues(result, nextCursor, previousCursor, hasNext, hasPrevious);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Result object must have nextCursor(), previousCursor(), hasNext(), and hasPrevious() methods", e);
        }
    }

    public static <T extends QueryResult> ResponseEntity<PagedResponse<T>> createPaginatedResponse(
            T result, String nextCursor, String previousCursor, Boolean hasNext, Boolean hasPrevious) {
        return createPaginatedResponseWithValues(result, nextCursor, previousCursor, hasNext, hasPrevious);
    }

    private static <T extends QueryResult> ResponseEntity<PagedResponse<T>> createPaginatedResponseWithValues(
            T result, String nextCursor, String previousCursor, Boolean hasNext, Boolean hasPrevious) {

        CursorPaginationInfo paginationInfo = CursorPaginationInfo.of(nextCursor, previousCursor, hasNext, hasPrevious);

        HttpHeaders headers = new HttpHeaders();
        if (nextCursor != null) {
            headers.add(StandardHeaders.X_PAGINATION_NEXT_CURSOR.toString(), nextCursor);
        }
        if (previousCursor != null) {
            headers.add(StandardHeaders.X_PAGINATION_PREVIOUS_CURSOR.toString(), previousCursor);
        }

        return ResponseEntity.ok().headers(headers).body(PagedResponse.success(result, paginationInfo));
    }

    public static Sort buildSort(
            ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPageRequest pageRequest) {
        Sort.Direction direction = pageRequest.direction()
                        == ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPageRequest.SortDirection
                                .ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, pageRequest.sortBy()).and(Sort.by(Sort.Direction.DESC, "id"));
    }

    public static Sort buildCursorSort() {
        return Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
    }
}
