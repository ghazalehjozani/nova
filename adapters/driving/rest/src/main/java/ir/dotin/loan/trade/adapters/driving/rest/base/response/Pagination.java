package ir.dotin.loan.trade.adapters.driving.rest.base.response;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Pagination<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious) {

    public Pagination(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this(
                content == null ? Collections.emptyList() : List.copyOf(content),
                pageNumber,
                pageSize,
                totalElements,
                calculateTotalPages(pageSize, totalElements),
                pageNumber == 0,
                isLastPage(pageNumber, pageSize, totalElements),
                hasNextPage(pageNumber, pageSize, totalElements),
                pageNumber > 0);
    }

    public static <T> Pagination<T> empty(int pageNumber, int pageSize) {
        return new Pagination<>(Collections.emptyList(), pageNumber, pageSize, 0);
    }

    @JsonIgnore
    public int numberOfElements() {
        return content.size();
    }

    @JsonIgnore
    public boolean hasContent() {
        return !content.isEmpty();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public <R> Pagination<R> map(Function<? super T, ? extends R> mapper) {
        List<R> mappedContent = (List<R>) content.stream().map(mapper).toList();
        return new Pagination<>(mappedContent, pageNumber, pageSize, totalElements);
    }

    private static int calculateTotalPages(int pageSize, long totalElements) {
        return pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
    }

    private static boolean isLastPage(int pageNumber, int pageSize, long totalElements) {
        int totalPages = calculateTotalPages(pageSize, totalElements);
        return pageNumber >= totalPages - 1 || totalPages == 0;
    }

    private static boolean hasNextPage(int pageNumber, int pageSize, long totalElements) {
        int totalPages = calculateTotalPages(pageSize, totalElements);
        return pageNumber < totalPages - 1;
    }
}
