package ir.dotin.loan.trade.adapters.driving.rest.base.response;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.trade.adapters.driving.rest.base.ServiceError;

@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface ServiceResponse<T> permits DataResponse, PagedResponse, EventStreamResponse, ErrorResponse {

    @Deprecated
    @JsonProperty("RsCode")
    String rsCode();

    @Deprecated
    @JsonProperty("IsSuccess")
    Boolean isSuccess();

    T data();

    List<ServiceError> errors();

    @JsonIgnore
    default boolean isSuccessful() {
        return errors() == null || errors().isEmpty();
    }

    @JsonIgnore
    default boolean hasErrors() {
        return !isSuccessful();
    }

    @JsonIgnore
    default ServiceError firstError() {
        return hasErrors() ? errors().getFirst() : null;
    }

    @JsonIgnore
    default List<ServiceError> safeErrors() {
        return errors() == null ? Collections.emptyList() : List.copyOf(errors());
    }

    @JsonIgnore
    <R> ServiceResponse<R> map(Function<? super T, ? extends R> mapper);

    @JsonIgnore
    <R> ServiceResponse<R> flatMap(Function<? super T, ? extends ServiceResponse<R>> mapper);

    @JsonIgnore
    default ResponseEntity<ServiceResponse<T>> toResponseEntity(HttpStatus status) {
        return ResponseEntity.status(status).body(this);
    }
}
