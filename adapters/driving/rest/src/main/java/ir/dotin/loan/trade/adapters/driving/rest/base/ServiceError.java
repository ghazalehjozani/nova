package ir.dotin.loan.trade.adapters.driving.rest.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import ir.dotin.platform.commons.core.i18n.LocalizedMessage;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Standard error structure for Dotin inter-service communication. Error code format: [PREFIX]-[SEQUENCE]
 *
 * <p>- PREFIX: 2-6 characters (service/product identifier) - SEQUENCE: 4 digits (0001-9999) - Reserved: 0001-0100 for
 * common errors - Available: 0101-9999 for service-specific errors
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServiceError(String code, String message, List<ErrorDetail> details) {

    public static ServiceError of(String code, String message) {
        return ServiceError.builder().code(code).message(message).build();
    }

    public static ServiceError of(String code, String message, List<ErrorDetail> details) {
        return ServiceError.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();
    }

    public static ServiceError of(String code, String message, ErrorDetail detail) {
        return of(code, message, Collections.singletonList(detail));
    }

    @Override
    public List<ErrorDetail> details() {
        return details == null ? Collections.emptyList() : Collections.unmodifiableList(details);
    }

    public boolean hasDetails() {
        return details != null && !details.isEmpty();
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(String field, String message, Map<String, Object> context) {

        public static ErrorDetail of(String field, String message) {
            return ErrorDetail.builder().field(field).message(message).build();
        }

        public static ErrorDetail of(String field, String message, Map<String, Object> context) {
            return ErrorDetail.builder()
                    .field(field)
                    .message(message)
                    .context(context != null ? Collections.unmodifiableMap(context) : null)
                    .build();
        }

        public static ErrorDetail of(String message) {
            return ErrorDetail.builder().message(message).build();
        }
    }

}
