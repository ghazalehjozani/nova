package ir.dotin.loan.trade.adapters.driving.rest.base.response;

import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;

import ir.dotin.loan.trade.adapters.driving.rest.base.ServiceError;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DataResponse<T>(String rsCode, Boolean isSuccess, T data, List<ServiceError> errors)
        implements ServiceResponse<T> {

    public DataResponse {
        errors = errors == null ? null : List.copyOf(errors);
    }

    public static <T> DataResponse<T> success(T data) {
        return new DataResponse<>("0", true, data, null);
    }

    public static <T> DataResponse<T> success() {
        return new DataResponse<>("0", true, null, null);
    }

    public static <T> DataResponse<T> failure(ServiceError error) {
        return new DataResponse<>("1", false, null, List.of(error));
    }

    public static <T> DataResponse<T> failure(List<ServiceError> errors) {
        return new DataResponse<>("1", false, null, errors);
    }

    public static <T> DataResponse<T> failure(String code, String message) {
        return failure(ServiceError.of(code, message));
    }

    @Override
    public <R> ServiceResponse<R> map(Function<? super T, ? extends R> mapper) {
        if (hasErrors()) {
            return new DataResponse<>(rsCode, isSuccess, null, errors);
        }
        return data == null ? new DataResponse<>(rsCode, isSuccess, null, null) : success(mapper.apply(data));
    }

    @Override
    public <R> ServiceResponse<R> flatMap(Function<? super T, ? extends ServiceResponse<R>> mapper) {
        if (hasErrors()) {
            return new DataResponse<>(rsCode, isSuccess, null, errors);
        }
        return data == null ? new DataResponse<>(rsCode, isSuccess, null, null) : mapper.apply(data);
    }
}
