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

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.envelope.api.AccountabilityIdentity;
import ir.dotin.platform.pangaea.envelope.api.AccountabilityType;
import ir.dotin.platform.pangaea.envelope.api.ActorEnvelope;
import ir.dotin.platform.pangaea.envelope.api.ActorEnvelopeCodec;
import ir.dotin.platform.pangaea.envelope.api.ActorEnvelopeFactory;
import ir.dotin.platform.pangaea.envelope.api.ActorEnvelopeSigner;
import ir.dotin.platform.pangaea.envelope.api.ExecutionMode;
import ir.dotin.platform.pangaea.envelope.api.ExecutionTrigger;
import ir.dotin.platform.pangaea.envelope.api.InitiatorIdentity;
import ir.dotin.platform.pangaea.envelope.api.InitiatorSource;
import ir.dotin.platform.pangaea.envelope.api.InitiatorType;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.platform.pangaea.security.api.OAuth2TokenResponse;
import ir.dotin.platform.pangaea.security.api.ServiceTokenProvider;
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
    private static final String HEADER_REQUEST_TIMESTAMP_EPOCH_MS = "X-Request-Timestamp-Epoch-Ms";
    private static final String HEADER_REQUEST_DEADLINE_EPOCH_MS = "X-Request-Deadline-Epoch-Ms";
    private static final String HEADER_HOST = "X-Host";
    private static final String ACCEPT_LANGUAGE_FA = "fa";
    private static final String PRODUCER_CODE = "NOVA";

    /**
     * Floor for a per-attempt reply timeout. If less than this remains in the total wall-time budget, the attempt is
     * not started (sending with a sub-100ms reply window would almost certainly time out anyway and just waste a round
     * trip to the broker).
     */
    private static final Duration MIN_ATTEMPT_TIMEOUT = Duration.ofMillis(100);

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
        // eventUid is generated ONCE here and reused as request key + eventUid header + Idempotency-Key on every retry
        // attempt, so retries are idempotent on the FCB side. Do not move this into the retry body.
        request.setEventUid(UUID.randomUUID().toString());
        request.setDateTime(Date.from(ZonedDateTime.now().toInstant()));
        request.setVersion(1);

        String operationType = request.getOperationName();
        Result<?> gateDecision = healthGate.checkPermitted(operationType);
        if (gateDecision != null && gateDecision.isFailure()) {
            healthMetrics.recordGateRejection();
            return Result.failure(gateDecision.err().orElseThrow());
        }

        // Bound the TOTAL wall-clock time of this request/reply call (initial attempt + all retries + backoff). The
        // RetryTemplate would otherwise re-pay the full per-call reply timeout on every attempt (~3x worst case). We
        // compute a single deadline up front and shrink each retry's reply timeout to the remaining budget below.
        long startNanos = System.nanoTime();
        long budgetNanos = computeBudgetNanos(timeout);

        try {
            return retryTemplate.execute(() -> {
                OAuth2TokenResponse token = serviceTokenProvider.getServiceToken();
                String bearerValue = buildBearerHeader(token);
                ActorEnvelope envelope = buildEnvelope();
                String signedEnvelope = envelopeSigner.sign(envelope);
                Duration attemptTimeout = remainingAttemptTimeout(timeout, startNanos, budgetNanos);
                return executeRequest(request, operationType, attemptTimeout, bearerValue, signedEnvelope);
            });
        } catch (RetryException e) {
            return mapRetryException(e, operationType, timeout);
        }
    }

    /**
     * Total wall-time budget for the whole call. Defaults to {@code perCallTimeout x retryBudgetMultiplier} (so retries
     * add at most "one extra attempt"); an explicit positive {@code retryMaxElapsed} caps it further (whichever is
     * smaller wins). The budget never drops below a single per-call timeout, so the initial attempt always gets its
     * full timeout.
     */
    private long computeBudgetNanos(Duration timeout) {
        long perCallNanos = timeout.toNanos();
        long derivedNanos = (long) (perCallNanos * properties.getRetryBudgetMultiplier());
        long budgetNanos = derivedNanos;
        Duration cap = properties.getRetryMaxElapsed();
        if (cap != null && !cap.isZero() && !cap.isNegative()) {
            budgetNanos = Math.min(budgetNanos, cap.toNanos());
        }
        return Math.max(budgetNanos, perCallNanos);
    }

    /**
     * Reply timeout for the current attempt: the smaller of the per-call timeout and the wall-time remaining in the
     * budget. Throws {@link TimeoutException} (retryable, but the next budget check will also fail) when the budget is
     * already spent, so a doomed attempt is never started — that is what prevents a retried timeout from re-paying
     * another full per-call timeout.
     */
    private Duration remainingAttemptTimeout(Duration timeout, long startNanos, long budgetNanos)
            throws TimeoutException {
        long remainingNanos = budgetNanos - (System.nanoTime() - startNanos);
        if (remainingNanos < MIN_ATTEMPT_TIMEOUT.toNanos()) {
            throw new TimeoutException("FCB request/reply wall-time budget exhausted before next attempt");
        }
        long attemptNanos = Math.min(timeout.toNanos(), remainingNanos);
        return Duration.ofNanos(attemptNanos);
    }

    private Result<FcbKafkaBaseResponse> mapRetryException(RetryException e, String operationType, Duration timeout) {
        Throwable cause = e.getCause();
        if (cause == null) {
            requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_OTHER);
            return Result.failure(CoreBankingErrors.KAFKA_COMMUNICATION_ERROR, "retry exhausted: " + e.getMessage());
        }
        return switch (cause) {
            case FcbServerException fse -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_SERVER);
                yield Result.failure(
                        CoreBankingErrors.KAFKA_FCB_SERVER_ERROR, fse.getErrorCode(), fse.getErrorMessage());
            }
            case TimeoutException ignored -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_TIMEOUT);
                requestReplyMetrics.recordDiscarded(operationType);
                yield Result.failure(
                        CoreBankingErrors.KAFKA_REPLY_TIMEOUT, operationType, String.valueOf(timeout.toMillis()));
            }
            case org.springframework.kafka.KafkaException ke -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_BROKER);
                yield Result.failure(CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE, ke.getMessage());
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
                        KafkaHeaders.REPLY_TOPIC, properties.getReplyTopic().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        KafkaHeaders.REPLY_PARTITION,
                        ByteBuffer.allocate(Integer.BYTES)
                                .putInt(replyPartition)
                                .array()));
        envelopeCodec.write(
                (name, value) -> record.headers().add(new RecordHeader(name, value.getBytes(StandardCharsets.UTF_8))),
                signedEnvelope);
        // No manual traceparent injection: the ReplyingKafkaTemplate now has Micrometer observation enabled
        // (KafkaObservationBeanPostProcessor in the platform kafka starter), so Spring Kafka stamps the W3C
        // traceparent from the active span — the same header name and format FCB expects.

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
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, operationType);
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
