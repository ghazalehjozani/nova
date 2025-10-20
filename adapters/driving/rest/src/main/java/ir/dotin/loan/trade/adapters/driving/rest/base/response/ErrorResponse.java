package ir.dotin.loan.trade.adapters.driving.rest.base.response;

import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;

import ir.dotin.loan.trade.adapters.driving.rest.base.ServiceError;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String rsCode, Boolean isSuccess, Void data, List<ServiceError> errors)
        implements ServiceResponse<Void> {

    public ErrorResponse {
        errors = errors == null ? null : List.copyOf(errors);
    }

    public ErrorResponse(List<ServiceError> errors) {
        this("1", false, null, List.copyOf(errors));
    }

    public static ErrorResponse of(ServiceError error) {
        return new ErrorResponse(List.of(error));
    }

    public static ErrorResponse of(List<ServiceError> errors) {
        return new ErrorResponse(errors);
    }

    public static ErrorResponse of(String code, String message) {
        return of(ServiceError.of(code, message));
    }

    @Override
    public <R> ServiceResponse<R> map(Function<? super Void, ? extends R> mapper) {
        return new DataResponse<>(rsCode, isSuccess, null, errors);
    }

    @Override
    public <R> ServiceResponse<R> flatMap(Function<? super Void, ? extends ServiceResponse<R>> mapper) {
        return new DataResponse<>(rsCode, isSuccess, null, errors);
    }
}
