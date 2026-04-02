package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;
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

import ir.dotin.platform.adapter.messaging.header.NovaHeader;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.security.OAuth2TokenResponse;
import ir.dotin.platform.commons.security.TokenClientService;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbResilienceConfig;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbSerializationException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbServerException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.KafkaErrorCodeMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@Profile("kafka-fcb")
public class FcbKafkaClient {

    private final ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FcbKafkaProperties properties;
    private final TokenClientService tokenClientService;
    private final RetryTemplate retryTemplate;
    private final CircuitBreaker circuitBreaker;
    private final Tracer tracer;

    public FcbKafkaClient(
            ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate,
            ObjectMapper objectMapper,
            FcbKafkaProperties properties,
            TokenClientService tokenClientService,
            @Qualifier(FcbResilienceConfig.FCB_KAFKA_RETRY_TEMPLATE) RetryTemplate retryTemplate,
            CircuitBreakerRegistry circuitBreakerRegistry,
            @Nullable Tracer tracer) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.tokenClientService = tokenClientService;
        this.retryTemplate = retryTemplate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(FcbResilienceConfig.FCB_KAFKA_CB);
        this.tracer = tracer;
    }

    public Result<FcbKafkaBaseResponse> sendAndReceive(FcbKafkaBaseRequest request, Duration timeout) {
        request.setProducerCode("NOVA");
        request.setEventUid(UUID.randomUUID().toString());
        request.setDateTime(Date.from(ZonedDateTime.now().toInstant()));
        request.setVersion(1);

        String operationType = request.getOperationName();

        log.debug("Sending Kafka request: operation={}, eventUid={}", operationType, request.getEventUid());

        Callable<Result<FcbKafkaBaseResponse>> cbCallable =
                CircuitBreaker.decorateCallable(circuitBreaker, () -> executeRequest(request, operationType, timeout));

        try {
            return retryTemplate.execute(() -> cbCallable.call());
        } catch (RetryException e) {
            return mapRetryException(e, operationType, timeout);
        }
    }

    private Result<FcbKafkaBaseResponse> mapRetryException(RetryException e, String operationType, Duration timeout) {
        Throwable cause = e.getCause();
        if (cause instanceof FcbServerException fse) {
            log.error("FCB server error after retries: operation={}, code={}", operationType, fse.getErrorCode());
            return Result.failure(Notification.ofError(
                    CoreBankingErrors.KAFKA_FCB_SERVER_ERROR, fse.getErrorCode(), fse.getErrorMessage()));
        }
        if (cause instanceof java.util.concurrent.TimeoutException) {
            log.warn("Kafka reply timed out: operation={}, timeout={}ms", operationType, timeout.toMillis());
            return Result.failure(Notification.ofError(
                    CoreBankingErrors.KAFKA_REPLY_TIMEOUT, operationType, String.valueOf(timeout.toMillis())));
        }
        if (cause instanceof CallNotPermittedException) {
            log.error("FCB Kafka circuit breaker OPEN: operation={}", operationType);
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE, "Circuit breaker OPEN"));
        }
        if (cause instanceof org.springframework.kafka.KafkaException ke) {
            log.error("Kafka broker error: operation={}", operationType, ke);
            return Result.failure(Notification.ofError(CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE, ke.getMessage()));
        }
        if (cause instanceof FcbSerializationException fse) {
            log.error("Serialization error: operation={}", operationType, fse);
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_SERIALIZATION_ERROR, fse.getErrorMessage()));
        }
        log.error("Kafka communication error: operation={}", operationType, cause);
        return Result.failure(Notification.ofError(
                CoreBankingErrors.KAFKA_COMMUNICATION_ERROR, cause != null ? cause.getMessage() : e.getMessage()));
    }

    private Result<FcbKafkaBaseResponse> executeRequest(
            FcbKafkaBaseRequest request, String operationType, Duration timeout) throws Exception {
        byte[] requestBytes;
        try {
            requestBytes = objectMapper.writeValueAsBytes(request);
        } catch (tools.jackson.core.JacksonException e) {
            throw new FcbSerializationException("Failed to serialize request: " + e.getMessage());
        }

        OAuth2TokenResponse token = tokenClientService.delegateToken();

        ProducerRecord<String, byte[]> record =
                new ProducerRecord<>(properties.requestTopic(), request.getEventUid(), requestBytes);

        record.headers()
                .add(new RecordHeader(
                        NovaHeader.OPERATION_TYPE.getValue(), operationType.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("eventUid", request.getEventUid().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        NovaHeader.IDEMPOTENCY_KEY.getValue(),
                        request.getEventUid().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        NovaHeader.REQUEST_DATETIME.getValue(),
                        request.getDateTime().toString().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        NovaHeader.AUTHORIZATION.getValue(), ("Bearer " + token).getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(NovaHeader.ACCEPT_LANGUAGE.getValue(), "fa".getBytes(StandardCharsets.UTF_8)));

        addTracingHeaders(record);

        record.headers()
                .add(new RecordHeader(
                        KafkaHeaders.REPLY_TOPIC, properties.replyTopic().getBytes(StandardCharsets.UTF_8)));

        RequestReplyFuture<String, byte[], byte[]> future = replyingKafkaTemplate.sendAndReceive(record, timeout);
        ConsumerRecord<String, byte[]> replyRecord = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);

        if (replyRecord.value() == null || replyRecord.value().length == 0) {
            log.warn("Empty Kafka reply for operation={}", operationType);
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

            log.warn(
                    "Kafka FCB error response: operation={}, errorCode={}, errorMessage={}",
                    operationType,
                    errorCode,
                    errorMessage);

            if (KafkaErrorCodeMapper.isServerError(errorCode)) {
                throw new FcbServerException(errorCode, errorMessage);
            } else if (KafkaErrorCodeMapper.isClientError(errorCode)) {
                return Result.failure(
                        Notification.ofError(CoreBankingErrors.KAFKA_FCB_CLIENT_ERROR, errorCode, errorMessage));
            } else {
                return Result.failure(KafkaErrorCodeMapper.mapToNotification(response));
            }
        }

        log.debug("Kafka reply received: operation={}, correlationId={}", operationType, response.getCorrelationId());
        return Result.success(response);
    }

    private void addTracingHeaders(ProducerRecord<String, byte[]> record) {
        if (tracer == null || tracer.currentSpan() == null) {
            return;
        }
        io.micrometer.tracing.TraceContext ctx = tracer.currentSpan().context();
        String sampledFlag = Boolean.TRUE.equals(ctx.sampled()) ? "01" : "00";
        String traceparent = "00-" + ctx.traceId() + "-" + ctx.spanId() + "-" + sampledFlag;
        record.headers()
                .add(new RecordHeader(NovaHeader.TRACEPARENT.getValue(), traceparent.getBytes(StandardCharsets.UTF_8)));
    }
}
