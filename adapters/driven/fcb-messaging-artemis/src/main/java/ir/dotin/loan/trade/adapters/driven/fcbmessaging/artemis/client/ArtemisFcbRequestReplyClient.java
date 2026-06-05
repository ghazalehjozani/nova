package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client;

import java.time.Duration;
import java.util.UUID;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.messaging.requestreply.api.Reply;
import ir.dotin.platform.pangaea.messaging.requestreply.api.ReplyTransportException;
import ir.dotin.platform.pangaea.messaging.requestreply.api.RequestReplyClient;
import ir.dotin.platform.pangaea.messaging.requestreply.api.RequestReplyTimeoutException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config.ArtemisFcbProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.client.FcbRequestReplyClient;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbSerializationException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.FcbErrorCodeMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

public class ArtemisFcbRequestReplyClient implements FcbRequestReplyClient {

    private static final String PROP_OPERATION_TYPE = "operationType";
    private static final String PROP_IDEMPOTENCY_KEY = "idempotencyKey";

    private static final String METRIC_LATENCY = "fcb.artemis.request_reply.latency";
    private static final String TAG_OPERATION = "operation";
    private static final String TAG_OUTCOME = "outcome";
    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_FAILURE = "failure";

    private final RequestReplyClient client;
    private final ObjectMapper objectMapper;
    private final ArtemisFcbProperties properties;
    private final MeterRegistry meterRegistry;

    public ArtemisFcbRequestReplyClient(
            RequestReplyClient client,
            ObjectMapper objectMapper,
            ArtemisFcbProperties properties,
            MeterRegistry meterRegistry) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Result<FcbBaseResponse> sendAndReceive(FcbBaseRequest request, Duration timeout) {
        String op = request.getOperationName();
        long startNanos = System.nanoTime();
        boolean success = false;
        try {
            byte[] body = objectMapper.writeValueAsBytes(request);
            Reply reply = client.destination(properties.getRequestAddress())
                    .header(PROP_OPERATION_TYPE, op)
                    .header(PROP_IDEMPOTENCY_KEY, UUID.randomUUID().toString())
                    .timeout(timeout)
                    .exchange(body);
            Result<FcbBaseResponse> result = handleReply(reply.payloadAsString(), op);
            success = result.isSuccess();
            return result;
        } catch (RequestReplyTimeoutException e) {
            return Result.failure(CoreBankingErrors.FCB_REPLY_TIMEOUT, op, String.valueOf(timeout.toMillis()));
        } catch (ReplyTransportException e) {
            return Result.failure(
                    CoreBankingErrors.FCB_BROKER_UNAVAILABLE,
                    e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        } catch (FcbSerializationException e) {
            return Result.failure(Notification.ofError(CoreBankingErrors.FCB_SERIALIZATION_ERROR, e.getErrorMessage()));
        } finally {
            recordLatency(op, startNanos, success);
        }
    }

    private Result<FcbBaseResponse> handleReply(String payload, String operationType) {
        if (payload == null || payload.isBlank()) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, operationType);
        }

        FcbBaseResponse response;
        try {
            response = deserializeResponse(payload, operationType);
        } catch (JacksonException e) {
            throw new FcbSerializationException("Failed to deserialize response: " + e.getMessage());
        }

        if (response.isError()) {
            String errorCode = response.getErrorCode() != null ? response.getErrorCode() : "UNKNOWN";
            String errorMessage = response.getErrorMessage() != null ? response.getErrorMessage() : "No error message";
            if (FcbErrorCodeMapper.isServerError(errorCode)) {
                return Result.failure(CoreBankingErrors.FCB_SERVER_ERROR, errorCode, errorMessage);
            }
            if (FcbErrorCodeMapper.isClientError(errorCode)) {
                return Result.failure(
                        Notification.ofError(CoreBankingErrors.FCB_CLIENT_ERROR, errorCode, errorMessage));
            }
            return Result.failure(FcbErrorCodeMapper.mapToNotification(response));
        }
        return Result.success(response);
    }

    private FcbBaseResponse deserializeResponse(String payload, String operationType) {
        JsonNode root = objectMapper.readTree(payload);
        if (root instanceof ObjectNode object
                && (object.get("operationName") == null
                        || object.get("operationName").isNull())) {
            object.put("operationName", operationType);
        }
        return objectMapper.treeToValue(root, FcbBaseResponse.class);
    }

    private void recordLatency(String operationType, long startNanos, boolean success) {
        Timer.builder(METRIC_LATENCY)
                .tag(TAG_OPERATION, operationType)
                .tag(TAG_OUTCOME, success ? OUTCOME_SUCCESS : OUTCOME_FAILURE)
                .register(meterRegistry)
                .record(Duration.ofNanos(System.nanoTime() - startNanos));
    }
}
