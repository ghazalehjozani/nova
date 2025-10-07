package ir.dotin.loan.trade.adapters.driving.rest.base;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

/**
 * Standard service response wrapper aligned with Dotin inter-service communication standards. Thread-safe immutable
 * value object.
 *
 * @param <T> Type of the data payload
 * @param rsCode
 * @param isSuccess
 * @param data Business data payload. Present only on successful operations.
 * @param errors List of errors. Present only when errors occur.
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServiceResponse<T>(
        @Deprecated(forRemoval = false) @JsonProperty("RsCode") String rsCode,
        @Deprecated(forRemoval = false) @JsonProperty("IsSuccess") Boolean isSuccess,
        T data,
        List<ServiceError> errors) {

    public static <T> ServiceResponse<T> success(T data) {
        return ServiceResponse.<T>builder()
                .rsCode("0")
                .isSuccess(true)
                .data(data)
                .build();
    }

    public static <T> ServiceResponse<T> success() {
        return ServiceResponse.<T>builder().rsCode("0").isSuccess(true).build();
    }

    public static <T> ServiceResponse<T> error(ServiceError error) {
        return ServiceResponse.<T>builder()
                .rsCode("1")
                .isSuccess(false)
                .errors(Collections.singletonList(error))
                .build();
    }

    public static <T> ServiceResponse<T> error(List<ServiceError> errors) {
        return ServiceResponse.<T>builder()
                .rsCode("1")
                .isSuccess(false)
                .errors(errors)
                .build();
    }

    public static <T> ServiceResponse<T> error(String code, String message) {
        return ServiceResponse.<T>builder()
                .rsCode("1")
                .isSuccess(false)
                .errors(Collections.singletonList(ServiceError.of(code, message)))
                .build();
    }

    public boolean isSuccessful() {
        return errors == null || errors.isEmpty();
    }

    public boolean hasErrors() {
        return !isSuccessful();
    }

    public ServiceError getFirstError() {
        return hasErrors() ? errors.getFirst() : null;
    }

    @Override
    public List<ServiceError> errors() {
        return errors == null ? Collections.emptyList() : Collections.unmodifiableList(errors);
    }

    public <R> ServiceResponse<R> map(Function<T, R> mapper) {
        return hasErrors()
                ? ServiceResponse.<R>builder().errors(this.errors).build()
                : ServiceResponse.success(mapper.apply(this.data));
    }

    public <R> ServiceResponse<R> flatMap(Function<T, ServiceResponse<R>> mapper) {
        return hasErrors() ? ServiceResponse.<R>builder().errors(this.errors).build() : mapper.apply(this.data);
    }
}
