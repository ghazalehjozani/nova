package ir.dotin.loan.trade.adapters.driving.rest.base;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

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
        ErrorCodeValidator.validate(code);
        return ServiceError.builder().code(code).message(message).build();
    }

    public static ServiceError of(String code, String message, List<ErrorDetail> details) {
        ErrorCodeValidator.validate(code);
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

    private static class ErrorCodeValidator {

        private static final String ERROR_CODE_PATTERN = "^[A-Z]{2,6}-\\d{4}$";

        static void validate(String code) {
            if (code == null || code.isEmpty()) {
                throw new IllegalArgumentException("Error code cannot be null or empty");
            }

            if (!code.matches(ERROR_CODE_PATTERN)) {
                throw new IllegalArgumentException(String.format(
                        "Invalid error code format: '%s'. Expected format: [PREFIX]-[SEQUENCE] "
                                + "where PREFIX is 2-6 uppercase letters and SEQUENCE is 4 digits",
                        code));
            }

            String[] parts = code.split("-");
            int sequence = Integer.parseInt(parts[1]);

            if (sequence < 1 || sequence > 9999) {
                throw new IllegalArgumentException(
                        String.format("Error code sequence must be between 0001 and 9999, got: %04d", sequence));
            }
        }

        static boolean isReservedCode(String code) {
            if (code == null || !code.contains("-")) {
                return false;
            }

            try {
                String sequence = code.split("-")[1];
                int seq = Integer.parseInt(sequence);
                return seq >= 1 && seq <= 100;
            } catch (RuntimeException ignored) {
                return false;
            }
        }
    }
}
