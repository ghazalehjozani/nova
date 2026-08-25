package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
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
import ir.dotin.platform.pangaea.security.api.ServiceTokenRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.client.FcbRequestReplyClient;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaConfig;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbResilienceConfig;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbSerializationException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbServerException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.FcbErrorCodeMapper;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.metrics.FcbRequestReplyMetrics;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.util.HostResolver;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import io.micrometer.tracing.Tracer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Component(FcbKafkaClient.KAFKA_FCB_CLIENT)
@ConditionalOnProperty(prefix = "nova.fcb.kafka", name = "enabled", matchIfMissing = true)
public class FcbKafkaClient implements FcbRequestReplyClient {

    /** Stable bean name so the composition-root router can {@code @Qualifier} the Kafka transport client. */
    public static final String KAFKA_FCB_CLIENT = "kafkaFcbRequestReplyClient";

    private static final String HEADER_OPERATION_TYPE = "X-Operation-Type";
    private static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";
    private static final String HEADER_REQUEST_DATETIME = "X-Request-DateTime";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String HEADER_HOST = "X-Host";
    private static final String ACCEPT_LANGUAGE_FA = "fa";

    /**
     * Floor for a per-attempt reply timeout. If less than this remains in the total wall-time budget, the attempt is
     * not started (sending with a sub-100ms reply window would almost certainly time out anyway and just waste a round
     * trip to the broker).
     */
    private static final Duration MIN_ATTEMPT_TIMEOUT = Duration.ofMillis(100);

    private final Clock clock;
    private final ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FcbKafkaProperties properties;
    private final ServiceTokenProvider serviceTokenProvider;
    private final AuthenticationContextHolder authenticationContextHolder;
    private final ActorEnvelopeFactory envelopeFactory;
    private final ActorEnvelopeSigner envelopeSigner;
    private final ActorEnvelopeCodec envelopeCodec;
    private final RetryTemplate retryTemplate;
    private final FcbRequestReplyMetrics requestReplyMetrics;
    private final @Nullable Tracer tracer;
    private final int replyPartition;

    /**
     * Drain state for graceful scale-down / lost-lease handling. Once {@code draining} is set, new
     * {@code sendAndReceive} calls fast-fail and {@link #awaitDrain(Duration)} blocks until the {@code inFlight} count
     * of outstanding request/reply calls reaches zero. Driven by {@code FcbReplyDrainCoordinator}.
     */
    private volatile boolean draining = false;

    private final AtomicInteger inFlight = new AtomicInteger();

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
            FcbRequestReplyMetrics requestReplyMetrics,
            @Qualifier(FcbKafkaConfig.FCB_INTEGRATION_REPLY_PARTITION) int replyPartition,
            @Nullable Tracer tracer,
            Clock clock) {
        this.clock = clock;
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.serviceTokenProvider = serviceTokenProvider;
        this.authenticationContextHolder = authenticationContextHolder;
        this.envelopeFactory = envelopeFactory;
        this.envelopeSigner = envelopeSigner;
        this.envelopeCodec = envelopeCodec;
        this.retryTemplate = retryTemplate;
        this.requestReplyMetrics = requestReplyMetrics;
        this.replyPartition = replyPartition;
        this.tracer = tracer;
    }

    @Override
    public Result<FcbBaseResponse> sendAndReceive(FcbBaseRequest request, Duration timeout) {
        if (draining) {
            return Result.failure(
                    CoreBankingErrors.FCB_BROKER_UNAVAILABLE, "instance is draining its FCB reply partition");
        }
        inFlight.incrementAndGet();
        try {
            return doSendAndReceive(request, timeout);
        } finally {
            inFlight.decrementAndGet();
        }
    }

    /** Marks this client draining: subsequent {@link #sendAndReceive} calls fast-fail. Idempotent. */
    public void beginDrain() {
        draining = true;
    }

    /** Number of request/reply calls currently in flight. */
    public int inFlightCount() {
        return inFlight.get();
    }

    /**
     * Blocks until no request/reply calls are in flight or {@code timeout} elapses. Returns {@code true} if fully
     * drained. Call {@link #beginDrain()} first so no new calls start while draining.
     */
    public boolean awaitDrain(Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (inFlight.get() > 0) {
            if (System.nanoTime() >= deadline) {
                return false;
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return inFlight.get() == 0;
            }
        }
        return true;
    }

    private Result<FcbBaseResponse> doSendAndReceive(FcbBaseRequest request, Duration timeout) {
        String operationType = request.getOperationName();
        String idempotencyKey = UUID.randomUUID().toString();

        // Bound the TOTAL wall-clock time of this request/reply call (initial attempt + all retries + backoff). The
        // RetryTemplate would otherwise re-pay the full per-call reply timeout on every attempt (~3x worst case). We
        // compute a single deadline up front and shrink each retry's reply timeout to the remaining budget below.
        long startNanos = System.nanoTime();
        long budgetNanos = computeBudgetNanos(timeout);

        try {
            return retryTemplate.execute(() -> {
                // Token mode follows the bound security context: an interactive (REST) call delegates the caller's
                // user token (AUTO), while a background path with no bound context — e.g. the @Scheduled reconciliation
                // sweep / converge — uses a client-credentials service token (async). Without this, AUTO under the
                // global FAIL_FAST ambiguous-context policy throws on the sweep thread and every recon-state call fails
                // as FCB_COMMUNICATION_ERROR (surfaced as a false "fcb-unreachable" UNKNOWN). Mirrors the outbox
                // poller pattern (KafkaEventPublisher uses ServiceTokenRequest.async()).
                ServiceTokenRequest tokenRequest =
                        authenticationContextHolder.authentication().isPresent()
                                ? ServiceTokenRequest.auto()
                                : ServiceTokenRequest.async();
                OAuth2TokenResponse token = serviceTokenProvider.getServiceToken(tokenRequest);
                String bearerValue = buildBearerHeader(token);
                byte[] requestBytes = serializeRequest(request);
                ActorEnvelope envelope = buildEnvelope();
                String signedEnvelope = envelopeSigner.sign(envelope, requestBytes);
                Duration attemptTimeout = remainingAttemptTimeout(timeout, startNanos, budgetNanos);
                return executeRequest(
                        operationType, idempotencyKey, attemptTimeout, bearerValue, signedEnvelope, requestBytes);
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

    private Result<FcbBaseResponse> mapRetryException(RetryException e, String operationType, Duration timeout) {
        Throwable cause = e.getCause();
        if (cause == null) {
            requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_OTHER);
            return Result.failure(CoreBankingErrors.FCB_COMMUNICATION_ERROR, "retry exhausted: " + e.getMessage());
        }
        return switch (cause) {
            case FcbServerException fse -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_SERVER);
                yield Result.failure(CoreBankingErrors.FCB_SERVER_ERROR, fse.getErrorCode(), fse.getErrorMessage());
            }
            case TimeoutException ignored -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_TIMEOUT);
                requestReplyMetrics.recordDiscarded(operationType);
                yield Result.failure(
                        CoreBankingErrors.FCB_REPLY_TIMEOUT, operationType, String.valueOf(timeout.toMillis()));
            }
            case org.springframework.kafka.KafkaException ke -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_BROKER);
                String message = ke.getMessage() != null
                        ? ke.getMessage()
                        : ke.getClass().getSimpleName();
                yield Result.failure(CoreBankingErrors.FCB_BROKER_UNAVAILABLE, message);
            }
            case FcbSerializationException fse -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_SERIALIZATION);
                yield Result.failure(
                        Notification.ofError(CoreBankingErrors.FCB_SERIALIZATION_ERROR, fse.getErrorMessage()));
            }
            default -> {
                requestReplyMetrics.recordPublisherFailure(operationType, FcbRequestReplyMetrics.REASON_OTHER);
                yield Result.failure(Notification.ofError(
                        CoreBankingErrors.FCB_COMMUNICATION_ERROR,
                        cause.getMessage() != null
                                ? cause.getMessage()
                                : cause.getClass().getSimpleName()));
            }
        };
    }

    private Result<FcbBaseResponse> executeRequest(
            String operationType,
            String idempotencyKey,
            Duration timeout,
            String bearerValue,
            String signedEnvelope,
            byte[] requestBytes)
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
            Result<FcbBaseResponse> result =
                    doExecuteRequest(operationType, idempotencyKey, timeout, bearerValue, signedEnvelope, requestBytes);
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

    private Result<FcbBaseResponse> doExecuteRequest(
            String operationType,
            String idempotencyKey,
            Duration timeout,
            String bearerValue,
            String signedEnvelope,
            byte[] requestBytes)
            throws Exception {

        long timestampMs = clock.millis();

        ProducerRecord<String, byte[]> record =
                new ProducerRecord<>(properties.getRequestTopic(), idempotencyKey, requestBytes);

        record.headers()
                .add(new RecordHeader(HEADER_OPERATION_TYPE, operationType.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(HEADER_IDEMPOTENCY_KEY, idempotencyKey.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        HEADER_REQUEST_DATETIME,
                        Instant.ofEpochMilli(timestampMs).toString().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(HEADER_AUTHORIZATION, bearerValue.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(HEADER_ACCEPT_LANGUAGE, ACCEPT_LANGUAGE_FA.getBytes(StandardCharsets.UTF_8)))
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
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, operationType);
        }

        FcbBaseResponse response;
        try {
            response = deserializeResponse(replyRecord.value(), operationType);
        } catch (tools.jackson.core.JacksonException e) {
            throw new FcbSerializationException("Failed to deserialize response: " + e.getMessage());
        }

        if (response.isError()) {
            String errorCode = response.getErrorCode() != null ? response.getErrorCode() : "UNKNOWN";
            String errorMessage = response.getErrorMessage() != null ? response.getErrorMessage() : "No error message";
            if (FcbErrorCodeMapper.isServerError(errorCode)) {
                throw new FcbServerException(errorCode, errorMessage);
            }
            if (FcbErrorCodeMapper.isClientError(errorCode)) {
                return Result.failure(
                        Notification.ofError(CoreBankingErrors.FCB_CLIENT_ERROR, errorCode, errorMessage));
            }
            return Result.failure(FcbErrorCodeMapper.mapToNotification(response));
        }

        return Result.success(response);
    }

    private FcbBaseResponse deserializeResponse(byte[] payload, String operationType) {
        JsonNode root = objectMapper.readTree(payload);
        if (root instanceof ObjectNode object
                && (object.get("operationName") == null
                        || object.get("operationName").isNull())) {
            object.put("operationName", operationType);
        }
        return objectMapper.treeToValue(root, FcbBaseResponse.class);
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

    private byte[] serializeRequest(FcbBaseRequest request) {
        try {
            return objectMapper.writeValueAsBytes(request);
        } catch (tools.jackson.core.JacksonException e) {
            throw new FcbSerializationException("Failed to serialize request: " + e.getMessage());
        }
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
