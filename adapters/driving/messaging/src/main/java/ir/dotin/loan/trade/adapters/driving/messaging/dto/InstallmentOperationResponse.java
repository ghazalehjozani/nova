package ir.dotin.loan.trade.adapters.driving.messaging.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * Response sent to the corridor response topic as part of the request/reply pattern.
 *
 * <p>The old system (Java 8) publishes a request to the request topic and
 * listens on the response topic for acknowledgment. This DTO is serialized
 * as JSON and published by Nova after command processing.</p>
 *
 * <p>The {@code eventUid} is the correlation key — it matches the
 * {@code eventUid} from the inbound request message.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InstallmentOperationResponse(
        String eventUid,
        String operationType,
        String fileNumber,
        Status status,
        Instant processedAt,
        @Nullable String message,
        @Nullable List<ErrorDetail> errors) {

    public enum Status {
        SUCCESS,
        FAILED
    }

    public record ErrorDetail(int code, String description) {}

    public static InstallmentOperationResponse success(
            String eventUid, String operationType, String fileNumber) {
        return new InstallmentOperationResponse(
                eventUid, operationType, fileNumber,
                Status.SUCCESS, Instant.now(), null, null);
    }

    public static InstallmentOperationResponse success(
            String eventUid, String operationType, String fileNumber, String message) {
        return new InstallmentOperationResponse(
                eventUid, operationType, fileNumber,
                Status.SUCCESS, Instant.now(), message, null);
    }

    public static InstallmentOperationResponse failed(
            String eventUid, String operationType, String fileNumber,
            String message, List<ErrorDetail> errors) {
        return new InstallmentOperationResponse(
                eventUid, operationType, fileNumber,
                Status.FAILED, Instant.now(), message, errors);
    }

    public static InstallmentOperationResponse failed(
            String eventUid, String operationType, String fileNumber, String message) {
        return new InstallmentOperationResponse(
                eventUid, operationType, fileNumber,
                Status.FAILED, Instant.now(), message, null);
    }
}
