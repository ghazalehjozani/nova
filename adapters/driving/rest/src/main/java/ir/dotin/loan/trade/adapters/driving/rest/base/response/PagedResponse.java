package ir.dotin.loan.trade.adapters.driving.rest.base.response;

import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import ir.dotin.loan.trade.adapters.driving.rest.base.ServiceError;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PagedResponse<T>(String rsCode, Boolean isSuccess, Pagination<T> data, List<ServiceError> errors)
        implements ServiceResponse<Pagination<T>> {

    public PagedResponse {
        errors = errors == null ? null : List.copyOf(errors);
    }

    public static <T> PagedResponse<T> success(Pagination<T> pagination) {
        return new PagedResponse<>("0", true, pagination, null);
    }

    public static <T> PagedResponse<T> success(List<T> content, int pageNumber, int pageSize, long totalElements) {
        return success(new Pagination<>(content, pageNumber, pageSize, totalElements));
    }

    public static <T> PagedResponse<T> empty(int pageNumber, int pageSize) {
        return success(Pagination.empty(pageNumber, pageSize));
    }

    public static <T> PagedResponse<T> failure(ServiceError error) {
        return new PagedResponse<>("1", false, null, List.of(error));
    }

    public static <T> PagedResponse<T> failure(List<ServiceError> errors) {
        return new PagedResponse<>("1", false, null, errors);
    }

    public static <T> PagedResponse<T> failure(String code, String message) {
        return failure(ServiceError.of(code, message));
    }

    @JsonIgnore
    public <R> PagedResponse<R> mapContent(Function<? super T, ? extends R> contentMapper) {
        if (hasErrors() || data == null) {
            return new PagedResponse<>(rsCode, isSuccess, null, errors);
        }
        return success(data.map(contentMapper));
    }

    @Override
    public <R> ServiceResponse<R> map(Function<? super Pagination<T>, ? extends R> mapper) {
        if (hasErrors()) {
            return new DataResponse<>(rsCode, isSuccess, null, errors);
        }
        return data == null
                ? new DataResponse<>(rsCode, isSuccess, null, null)
                : DataResponse.success(mapper.apply(data));
    }

    @Override
    public <R> ServiceResponse<R> flatMap(Function<? super Pagination<T>, ? extends ServiceResponse<R>> mapper) {
        if (hasErrors()) {
            return new DataResponse<>(rsCode, isSuccess, null, errors);
        }
        return data == null ? new DataResponse<>(rsCode, isSuccess, null, null) : mapper.apply(data);
    }
}
