package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InstallmentOperationResponse(
        String eventUid,
        String operationType,
        String fileNumber,
        ResponseStatus status,
        @Nullable String errorMessage,
        @Nullable Instant processedAt,
        @Nullable List<String> processedTransactionNumbers) {

    public enum ResponseStatus {
        SUCCESS,
        FAILED
    }

    public static InstallmentOperationResponse success(String eventUid, String operationType, String fileNumber) {
        return new InstallmentOperationResponse(
                eventUid, operationType, fileNumber, ResponseStatus.SUCCESS, null, Instant.now(), null);
    }

    public static InstallmentOperationResponse failed(
            String eventUid, String operationType, String fileNumber, String errorMessage) {
        return new InstallmentOperationResponse(
                eventUid, operationType, fileNumber, ResponseStatus.FAILED, errorMessage, Instant.now(), null);
    }
}
