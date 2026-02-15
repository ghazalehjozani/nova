package ir.dotin.loan.trade.adapters.driving.messaging.dto;

/**
 * SWA.101-compliant response structure for installment operations.
 *
 * <p>This V2 response aligns with the Inter-Service Communication Standards (SWA.101 v1.1).
 * It is currently unused because the old system (Java 8 / Corridor) expects the V1 format
 * ({@link InstallmentOperationResponse}). Once the old system is migrated or updated to
 * support the SWA.101 format, this class should replace V1.</p>
 *
 * <p><b>Wire format (SWA.101):</b></p>
 * <pre>{@code
 * {
 *   "header": {
 *     "Idempotency-Key": "a1b2c3...",
 *     "X-Request-DateTime": "2026-02-15T10:30:00Z",
 *     "X-Idempotency-Replayed": "false",
 *     "X-Response-DateTime": "2026-02-15T10:30:01Z",
 *     "X-Response-Code": "200"
 *   },
 *   "metadata": {},
 *   "resultData": {
 *     "eventUid": "a1b2c3...",
 *     "operationType": "INSTALLMENT_COLLECTION",
 *     "fileNumber": "1234567890",
 *     "processedAt": "2026-02-15T10:30:00Z"
 *   },
 *   "message": "Installment collected successfully",
 *   "errorList": []
 * }
 * }</pre>
 *
 * <p><b>Migration plan:</b></p>
 * <ol>
 *   <li>Old system consumer updated to parse SWA.101 BaseResponse format</li>
 *   <li>Replace {@link InstallmentOperationResponse} with this class</li>
 *   <li>Use platform's {@code CommandResponsePublisher} instead of
 *       {@link ir.dotin.loan.trade.adapters.driving.messaging.publisher.InstallmentOperationResponsePublisher}</li>
 *   <li>Headers embedded in body per SWA.101 section 4.3 (Corridor doesn't support Kafka headers)</li>
 * </ol>
 */
// @formatter:off
//
// import java.time.Instant;
// import java.util.List;
// import java.util.Map;
//
// import com.fasterxml.jackson.annotation.JsonInclude;
// import org.jspecify.annotations.Nullable;
//
// @JsonInclude(JsonInclude.Include.NON_NULL)
// public record InstallmentOperationResponseV2(
//         Map<String, String> header,
//         @Nullable Map<String, Object> metadata,
//         @Nullable ResultData resultData,
//         @Nullable String message,
//         @Nullable List<ErrorItem> errorList) {
//
//     public record ResultData(
//             String eventUid,
//             String operationType,
//             String fileNumber,
//             Instant processedAt) {}
//
//     public record ErrorItem(
//             String code,
//             String message) {}
//
//     public static InstallmentOperationResponseV2 success(
//             Map<String, String> headers,
//             String eventUid,
//             String operationType,
//             String fileNumber) {
//         return new InstallmentOperationResponseV2(
//                 headers,
//                 null,
//                 new ResultData(eventUid, operationType, fileNumber, Instant.now()),
//                 "Installment collected successfully",
//                 List.of());
//     }
//
//     public static InstallmentOperationResponseV2 failed(
//             Map<String, String> headers,
//             String message,
//             List<ErrorItem> errors) {
//         return new InstallmentOperationResponseV2(
//                 headers,
//                 null,
//                 null,
//                 message,
//                 errors);
//     }
// }
//
// @formatter:on
public final class InstallmentOperationResponseV2 {
    private InstallmentOperationResponseV2() {}
}
