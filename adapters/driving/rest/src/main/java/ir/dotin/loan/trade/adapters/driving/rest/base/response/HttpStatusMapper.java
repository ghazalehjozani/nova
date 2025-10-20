package ir.dotin.loan.trade.adapters.driving.rest.base.response;

import java.util.Map;

import org.springframework.http.HttpStatus;

public final class HttpStatusMapper {

    private static final Map<String, HttpStatus> STANDARD_ERROR_MAPPINGS = Map.ofEntries(
            Map.entry("*-0001", HttpStatus.BAD_REQUEST),
            Map.entry("*-0002", HttpStatus.UNAUTHORIZED),
            Map.entry("*-0003", HttpStatus.FORBIDDEN),
            Map.entry("*-0004", HttpStatus.TOO_EARLY),
            Map.entry("*-0005", HttpStatus.CONFLICT),
            Map.entry("*-0006", HttpStatus.TOO_MANY_REQUESTS));

    private static final Map<String, HttpStatus> PREFIX_MAPPINGS = Map.ofEntries(
            Map.entry("AUTH-", HttpStatus.UNAUTHORIZED),
            Map.entry("AUTHZ-", HttpStatus.FORBIDDEN),
            Map.entry("PERM-", HttpStatus.FORBIDDEN),
            Map.entry("NF-", HttpStatus.NOT_FOUND),
            Map.entry("NOT_FOUND-", HttpStatus.NOT_FOUND),
            Map.entry("CONFLICT-", HttpStatus.CONFLICT),
            Map.entry("VAL-", HttpStatus.BAD_REQUEST),
            Map.entry("VALIDATION-", HttpStatus.BAD_REQUEST),
            Map.entry("BIZ-", HttpStatus.UNPROCESSABLE_ENTITY),
            Map.entry("BUSINESS-", HttpStatus.UNPROCESSABLE_ENTITY),
            Map.entry("RATE_LIMIT-", HttpStatus.TOO_MANY_REQUESTS));

    private HttpStatusMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static HttpStatus fromErrorCode(String errorCode) {
        if (errorCode == null || errorCode.isBlank()) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return STANDARD_ERROR_MAPPINGS.getOrDefault(normalizeErrorCode(errorCode), determineStatusByPrefix(errorCode));
    }

    private static String normalizeErrorCode(String errorCode) {
        if (errorCode.matches("^[A-Z]+-\\d{4}$")) {
            return "*" + errorCode.substring(errorCode.lastIndexOf('-'));
        }
        return errorCode;
    }

    private static HttpStatus determineStatusByPrefix(String errorCode) {
        return PREFIX_MAPPINGS.entrySet().stream()
                .filter(entry -> errorCode.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
