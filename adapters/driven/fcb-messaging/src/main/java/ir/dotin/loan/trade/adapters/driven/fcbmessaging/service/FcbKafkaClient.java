package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import ir.dotin.platform.envelope.api.AccountabilityIdentity;
import ir.dotin.platform.envelope.api.AccountabilityType;
import ir.dotin.platform.envelope.api.ActorEnvelope;
import ir.dotin.platform.envelope.api.ActorEnvelopeCodec;
import ir.dotin.platform.envelope.api.ActorEnvelopeFactory;
import ir.dotin.platform.envelope.api.ActorEnvelopeSigner;
import ir.dotin.platform.envelope.api.ExecutionMode;
import ir.dotin.platform.envelope.api.ExecutionTrigger;
import ir.dotin.platform.envelope.api.InitiatorIdentity;
import ir.dotin.platform.envelope.api.InitiatorSource;
import ir.dotin.platform.envelope.api.InitiatorType;
import ir.dotin.platform.messaging.api.header.MessagingHeaderNames;
import ir.dotin.platform.messaging.api.version.ContractVersion;
import ir.dotin.platform.messaging.api.version.ContractVersionEnforcer;
import ir.dotin.platform.messaging.api.version.ContractVersionRegistry;
import ir.dotin.platform.messaging.api.version.VersionSkewDecision;
import ir.dotin.platform.security.api.AuthenticationContextHolder;
import ir.dotin.platform.security.api.OAuth2TokenResponse;
import ir.dotin.platform.security.api.ServiceTokenProvider;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaConfig;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbResilienceConfig;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbSerializationException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbServerException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.health.FcbHealthGate;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.health.FcbHealthMetrics;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.KafkaErrorCodeMapper;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.metrics.FcbRequestReplyMetrics;
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
    private final ServiceTokenProvider serviceTokenProvider;
    private final AuthenticationContextHolder authenticationContextHolder;
    private final ActorEnvelopeFactory envelopeFactory;
    private final ActorEnvelopeSigner envelopeSigner;
    private final ActorEnvelopeCodec envelopeCodec;
    private final RetryTemplate retryTemplate;
    private final FcbHealthGate healthGate;
    private final FcbHealthMetrics healthMetrics;
    private final FcbRequestReplyMetrics requestReplyMetrics;
    private final Tracer tracer;
    private final int replyPartition;

    public FcbKafkaClient(
            @Qualifier(FcbKafkaConfig.FCB_INTEGRATION_REPLYING_TEMPLATE)
                    ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate,
            ObjectMapper objectMapper,
            FcbKafkaProperties properties,
            ServiceTokenProvider serviceTokenProvider,
            AuthenticationContextHolder authenticationContextHolder,
            ActorEnvelopeFactory envelopeFactory,
            ActorEnvelopeSigner envelopeSigner,
            ActorEnvelopeCodec envelopeCodec,
            @Qualifier(FcbResilienceConfig.FCB_KAFKA_RETRY_TEMPLATE) RetryTemplate retryTemplate,
            FcbHealthGate healthGate,
            FcbHealthMetrics healthMetrics,
            FcbRequestReplyMetrics requestReplyMetrics,
            @Qualifier(FcbKafkaConfig.FCB_INTEGRATION_REPLY_PARTITION) int replyPartition,
            @Nullable Tracer tracer) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.serviceTokenProvider = serviceTokenProvider;
        this.authenticationContextHolder = authenticationContextHolder;
        this.envelopeFactory = envelopeFactory;
        this.envelopeSigner = envelopeSigner;
        this.envelopeCodec = envelopeCodec;
        this.retryTemplate = retryTemplate;
        this.healthGate = healthGate;
        this.healthMetrics = healthMetrics;
        this.requestReplyMetrics = requestReplyMetrics;
        this.replyPartition = replyPartition;
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
                OAuth2TokenResponse token = serviceTokenProvider.getServiceToken();
                String bearerValue = buildBearerHeader(token);
                ActorEnvelope envelope = buildEnvelope();
                String signedEnvelope = envelopeSigner.sign(envelope);
                return executeRequest(request, operationType, timeout, bearerValue, signedEnvelope);
            });
        } catch (RetryException e) {
            return mapRetryException(e, operationType, timeout);
        }
    }

    private Result<FcbKafkaBaseResponse> mapRetryException(RetryException e, String operationType, Duration timeout) {
        Throwable cause = e.getCause();
        if (cause == null) {
            requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_OTHER);
            return Result.failure(Notification.ofError(
                    CoreBankingErrors.KAFKA_COMMUNICATION_ERROR, "retry exhausted: " + e.getMessage()));
        }
        return switch (cause) {
            case FcbServerException fse -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_SERVER);
                yield Result.failure(Notification.ofError(
                        CoreBankingErrors.KAFKA_FCB_SERVER_ERROR, fse.getErrorCode(), fse.getErrorMessage()));
            }
            case TimeoutException ignored -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_TIMEOUT);
                requestReplyMetrics.recordDiscarded(operationType);
                yield Result.failure(Notification.ofError(
                        CoreBankingErrors.KAFKA_REPLY_TIMEOUT, operationType, String.valueOf(timeout.toMillis())));
            }
            case org.springframework.kafka.KafkaException ke -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_BROKER);
                yield Result.failure(Notification.ofError(CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE, ke.getMessage()));
            }
            case FcbSerializationException fse -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_SERIALIZATION);
                yield Result.failure(
                        Notification.ofError(CoreBankingErrors.KAFKA_SERIALIZATION_ERROR, fse.getErrorMessage()));
            }
            default -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_OTHER);
                yield Result.failure(Notification.ofError(
                        CoreBankingErrors.KAFKA_COMMUNICATION_ERROR,
                        cause.getMessage() != null
                                ? cause.getMessage()
                                : cause.getClass().getSimpleName()));
            }
        };
    }

    private Result<FcbKafkaBaseResponse> executeRequest(
            FcbKafkaBaseRequest request,
            String operationType,
            Duration timeout,
            String bearerValue,
            String signedEnvelope)
            throws Exception {

        io.micrometer.tracing.Span hop = null;
        Tracer.SpanInScope hopScope = null;
        if (tracer != null) {
            hop = tracer.spanBuilder()
                    .name("fcb-legacy " + operationType)
                    .kind(io.micrometer.tracing.Span.Kind.CLIENT)
                    .tag("peer.service", "fcb-legacy")
                    .tag("messaging.system", "kafka")
                    .tag("messaging.operation", "request_reply")
                    .tag("messaging.destination.name", properties.getRequestTopic())
                    .start();
            hopScope = tracer.withSpan(hop);
        }
        long startNanos = System.nanoTime();
        boolean success = false;
        try {
            Result<FcbKafkaBaseResponse> result =
                    doExecuteRequest(request, operationType, timeout, bearerValue, signedEnvelope);
            success = result.isSuccess();
            return result;
        } catch (Exception t) {
            if (hop != null) {
                hop.error(t);
            }
            throw t;
        } finally {
            String outcome = success ? FcbRequestReplyMetrics.OUTCOME_SUCCESS : FcbRequestReplyMetrics.OUTCOME_FAILURE;
            requestReplyMetrics.recordMatchDuration(
                    operationType, Duration.ofNanos(System.nanoTime() - startNanos), outcome);
            if (hopScope != null) {
                hopScope.close();
            }
            if (hop != null) {
                hop.end();
            }
        }
    }

    private Result<FcbKafkaBaseResponse> doExecuteRequest(
            FcbKafkaBaseRequest request,
            String operationType,
            Duration timeout,
            String bearerValue,
            String signedEnvelope)
            throws Exception {

        byte[] requestBytes;
        try {
            requestBytes = objectMapper.writeValueAsBytes(request);
        } catch (tools.jackson.core.JacksonException e) {
            throw new FcbSerializationException("Failed to serialize request: " + e.getMessage());
        }

        long timestampMs = System.currentTimeMillis();
        long deadlineMs = timestampMs + timeout.toMillis();

        ProducerRecord<String, byte[]> record =
                new ProducerRecord<>(properties.getRequestTopic(), request.getEventUid(), requestBytes);

        ContractVersion requestVersion = ContractVersionRegistry.requireAnnotation(request.getClass());

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
                        MessagingHeaderNames.CONTRACT_VERSION,
                        Integer.toString(requestVersion.value()).getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        MessagingHeaderNames.MIN_CONSUMER_VERSION,
                        Integer.toString(requestVersion.minConsumerVersion()).getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        KafkaHeaders.REPLY_TOPIC, properties.getReplyTopic().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        KafkaHeaders.REPLY_PARTITION,
                        ByteBuffer.allocate(Integer.BYTES)
                                .putInt(replyPartition)
                                .array()));
        envelopeCodec.write(
                (name, value) -> record.headers().add(new RecordHeader(name, value.getBytes(StandardCharsets.UTF_8))),
                signedEnvelope);
        addTracingHeaders(record);

        RequestReplyFuture<String, byte[], byte[]> future = replyingKafkaTemplate.sendAndReceive(record, timeout);

        ConsumerRecord<String, byte[]> replyRecord;
        try {
            replyRecord = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
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

        Result<FcbKafkaBaseResponse> versionCheck = verifyReplyContractVersion(replyRecord, response, operationType);
        if (versionCheck != null) {
            return versionCheck;
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

    private Result<FcbKafkaBaseResponse> verifyReplyContractVersion(
            ConsumerRecord<String, byte[]> replyRecord, FcbKafkaBaseResponse response, String operationType) {

        ContractVersion responseVersion = ContractVersionRegistry.requireAnnotation(response.getClass());
        Integer producerVersion = readIntHeader(replyRecord, MessagingHeaderNames.CONTRACT_VERSION);
        Integer minConsumerVersion = readIntHeader(replyRecord, MessagingHeaderNames.MIN_CONSUMER_VERSION);

        VersionSkewDecision decision = ContractVersionEnforcer.decide(
                producerVersion, minConsumerVersion, responseVersion.value(), responseVersion.minProducerVersion());

        if (decision == VersionSkewDecision.ACCEPT) {
            return null;
        }

        requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_OTHER);
        return Result.failure(Notification.ofError(
                CoreBankingErrors.KAFKA_FCB_VERSION_MISMATCH,
                decision.name(),
                operationType,
                producerVersion == null ? "null" : producerVersion.toString()));
    }

    private static Integer readIntHeader(ConsumerRecord<String, byte[]> record, String name) {
        var header = record.headers().lastHeader(name);
        if (header == null || header.value() == null) {
            return null;
        }
        String value = new String(header.value(), StandardCharsets.UTF_8).trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
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

    private ActorEnvelope buildEnvelope() {
        String userId = authenticationContextHolder.userId().orElse(null);
        if (userId != null) {
            String branchCode = authenticationContextHolder.branchCode().orElse(null);
            InitiatorIdentity initiator = new InitiatorIdentity(
                    userId, InitiatorType.HUMAN_USER, branchCode, InitiatorSource.SECURITY_CONTEXT);
            AccountabilityIdentity accountability = new AccountabilityIdentity(userId, AccountabilityType.HUMAN_USER);
            return envelopeFactory.fromExplicit(
                    initiator, accountability, ExecutionTrigger.REST_REQUEST, ExecutionMode.SYNC);
        }
        return envelopeFactory.fromConfigDefault(
                InitiatorType.SYSTEM_RECOVERY, "system", null, ExecutionTrigger.RECOVERY, ExecutionMode.SYNC);
    }
}
