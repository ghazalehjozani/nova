package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.security.api.OAuth2TokenResponse;
import ir.dotin.platform.security.api.TokenClientService;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbResilienceConfig;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbSerializationException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbServerException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.health.FcbHealthGate;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.health.FcbHealthMetrics;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.health.FcbPartitionHealthRegistry;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.KafkaErrorCodeMapper;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.util.HostResolver;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import tools.jackson.databind.ObjectMapper;

@Component
@Profile("kafka-fcb")
public class FcbKafkaClient {

    private static final String HEADER_OPERATION_TYPE = "X-Operation-Type";
    private static final String HEADER_EVENT_UID = "eventUid";
    private static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";
    private static final String HEADER_REQUEST_DATETIME = "X-Request-DateTime";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String HEADER_TRACEPARENT = "traceparent";
    private static final String HEADER_REQUEST_TIMESTAMP_EPOCH_MS = "X-Request-Timestamp-Epoch-Ms";
    private static final String HEADER_REQUEST_DEADLINE_EPOCH_MS = "X-Request-Deadline-Epoch-Ms";
    private static final String HEADER_HOST = "X-Host";
    private static final String ACCEPT_LANGUAGE_FA = "fa";
    private static final String PRODUCER_CODE = "NOVA";

    private final ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FcbKafkaProperties properties;
    private final TokenClientService tokenClientService;
    private final RetryTemplate retryTemplate;
    private final FcbHealthGate healthGate;
    private final FcbHealthMetrics healthMetrics;
    private final FcbPartitionHealthRegistry partitionRegistry;
    private final Tracer tracer;

    public FcbKafkaClient(
            ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate,
            ObjectMapper objectMapper,
            FcbKafkaProperties properties,
            TokenClientService tokenClientService,
            @Qualifier(FcbResilienceConfig.FCB_KAFKA_RETRY_TEMPLATE) RetryTemplate retryTemplate,
            FcbHealthGate healthGate,
            FcbHealthMetrics healthMetrics,
            FcbPartitionHealthRegistry partitionRegistry,
            @Nullable Tracer tracer) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.tokenClientService = tokenClientService;
        this.retryTemplate = retryTemplate;
        this.healthGate = healthGate;
        this.healthMetrics = healthMetrics;
        this.partitionRegistry = partitionRegistry;
        this.tracer = tracer;
    }

    public Result<FcbKafkaBaseResponse> sendAndReceive(FcbKafkaBaseRequest request, Duration timeout) {
        request.setProducerCode(PRODUCER_CODE);
        request.setEventUid(UUID.randomUUID().toString());
        request.setDateTime(Date.from(ZonedDateTime.now().toInstant()));
        request.setVersion(1);

        String operationType = request.getOperationName();
        Result<Void> gateDecision = healthGate.checkPermitted(operationType);
        if (gateDecision != null && gateDecision.isFailure()) {
            healthMetrics.recordGateRejection();
            return Result.failure(gateDecision.notification());
        }

        try {
            return retryTemplate.execute(() -> {
                OAuth2TokenResponse token = tokenClientService.delegateToken();
                String bearerValue = buildBearerHeader(token);
                return executeRequest(request, operationType, timeout, bearerValue);
            });
        } catch (RetryException e) {
            return mapRetryException(e, operationType, timeout);
        }
    }

    private Result<FcbKafkaBaseResponse> mapRetryException(RetryException e, String operationType, Duration timeout) {
        Throwable cause = e.getCause();
        if (cause == null) {
            return Result.failure(Notification.ofError(
                    CoreBankingErrors.KAFKA_COMMUNICATION_ERROR, "retry exhausted: " + e.getMessage()));
        }
        return switch (cause) {
            case FcbServerException fse ->
                Result.failure(Notification.ofError(
                        CoreBankingErrors.KAFKA_FCB_SERVER_ERROR, fse.getErrorCode(), fse.getErrorMessage()));
            case java.util.concurrent.TimeoutException ignored ->
                Result.failure(Notification.ofError(
                        CoreBankingErrors.KAFKA_REPLY_TIMEOUT, operationType, String.valueOf(timeout.toMillis())));
            case org.springframework.kafka.KafkaException ke ->
                Result.failure(Notification.ofError(CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE, ke.getMessage()));
            case FcbSerializationException fse ->
                Result.failure(
                        Notification.ofError(CoreBankingErrors.KAFKA_SERIALIZATION_ERROR, fse.getErrorMessage()));
            default ->
                Result.failure(Notification.ofError(
                        CoreBankingErrors.KAFKA_COMMUNICATION_ERROR,
                        cause.getMessage() != null
                                ? cause.getMessage()
                                : cause.getClass().getSimpleName()));
        };
    }

    private Result<FcbKafkaBaseResponse> executeRequest(
            FcbKafkaBaseRequest request, String operationType, Duration timeout, String bearerValue) throws Exception {

        byte[] requestBytes;
        try {
            requestBytes = objectMapper.writeValueAsBytes(request);
        } catch (tools.jackson.core.JacksonException e) {
            throw new FcbSerializationException("Failed to serialize request: " + e.getMessage());
        }

        int targetPartition = partitionRegistry.choosePartition(request.getEventUid());
        if (targetPartition == -1) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE, "No healthy partition available"));
        }

        long timestampMs = System.currentTimeMillis();
        long deadlineMs = timestampMs + timeout.toMillis();

        ProducerRecord<String, byte[]> record =
                new ProducerRecord<>(properties.requestTopic(), targetPartition, request.getEventUid(), requestBytes);

        record.headers()
                .add(new RecordHeader(HEADER_OPERATION_TYPE, operationType.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(HEADER_EVENT_UID, request.getEventUid().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        HEADER_IDEMPOTENCY_KEY, request.getEventUid().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        HEADER_REQUEST_DATETIME,
                        request.getDateTime().toInstant().toString().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(HEADER_AUTHORIZATION, bearerValue.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(HEADER_ACCEPT_LANGUAGE, ACCEPT_LANGUAGE_FA.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        HEADER_REQUEST_TIMESTAMP_EPOCH_MS,
                        Long.toString(timestampMs).getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        HEADER_REQUEST_DEADLINE_EPOCH_MS,
                        Long.toString(deadlineMs).getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        HEADER_HOST, HostResolver.resolveHostName().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        KafkaHeaders.REPLY_TOPIC, properties.replyTopic().getBytes(StandardCharsets.UTF_8)));

        addTracingHeaders(record);

        RequestReplyFuture<String, byte[], byte[]> future = replyingKafkaTemplate.sendAndReceive(record, timeout);

        ConsumerRecord<String, byte[]> replyRecord;
        try {
            replyRecord = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw e;
        }

        if (replyRecord.value() == null || replyRecord.value().length == 0) {
            return Result.failure(Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, operationType));
        }

        FcbKafkaBaseResponse response;
        try {
            response = objectMapper.readValue(replyRecord.value(), FcbKafkaBaseResponse.class);
        } catch (tools.jackson.core.JacksonException e) {
            throw new FcbSerializationException("Failed to deserialize response: " + e.getMessage());
        }

        if (response.isError()) {
            String errorCode = response.getErrorCode() != null ? response.getErrorCode() : "UNKNOWN";
            String errorMessage = response.getErrorMessage() != null ? response.getErrorMessage() : "No error message";
            if (KafkaErrorCodeMapper.isServerError(errorCode)) {
                throw new FcbServerException(errorCode, errorMessage);
            }
            if (KafkaErrorCodeMapper.isClientError(errorCode)) {
                return Result.failure(
                        Notification.ofError(CoreBankingErrors.KAFKA_FCB_CLIENT_ERROR, errorCode, errorMessage));
            }
            return Result.failure(KafkaErrorCodeMapper.mapToNotification(response));
        }

        return Result.success(response);
    }

    private String buildBearerHeader(OAuth2TokenResponse token) {
        if (token == null) {
            throw new IllegalStateException("TokenClientService returned null token");
        }
        String accessToken = token.accessToken();
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("OAuth2TokenResponse contains blank accessToken");
        }
        return "Bearer " + accessToken;
    }

    private void addTracingHeaders(ProducerRecord<String, byte[]> record) {
        if (tracer == null || tracer.currentSpan() == null) {
            return;
        }
        TraceContext ctx = tracer.currentSpan().context();
        String sampledFlag = Boolean.TRUE.equals(ctx.sampled()) ? "01" : "00";
        String traceparent = "00-" + ctx.traceId() + "-" + ctx.spanId() + "-" + sampledFlag;
        record.headers().add(new RecordHeader(HEADER_TRACEPARENT, traceparent.getBytes(StandardCharsets.UTF_8)));
    }
}
