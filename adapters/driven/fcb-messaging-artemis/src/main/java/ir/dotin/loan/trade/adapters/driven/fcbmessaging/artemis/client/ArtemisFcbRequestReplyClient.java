package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TextMessage;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.envelope.api.AccountabilityIdentity;
import ir.dotin.platform.pangaea.envelope.api.AccountabilityType;
import ir.dotin.platform.pangaea.envelope.api.ActorEnvelope;
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
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config.ArtemisFcbProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.client.FcbRequestReplyClient;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbSerializationException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbServerException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.KafkaErrorCodeMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * ActiveMQ Artemis implementation of the transport-neutral {@link FcbRequestReplyClient} seam.
 *
 * <p>Each call serialises the polymorphic {@link FcbKafkaBaseRequest} to JSON (the SAME body the Kafka path sends — FCB
 * routes by the {@code operationName} discriminator into the SAME dispatcher), stamps the pinned JMS string properties,
 * creates a JMS <em>temporary</em> reply queue set as {@code JMSReplyTo} with {@code JMSCorrelationID = eventUid},
 * sends to the configured request address, and synchronously {@code receive(timeout)} on the temporary queue. Because a
 * temporary queue is bound to this connection, the broker delivers the reply only to this originating Nova instance —
 * so the Kafka reply-partition lease is unnecessary on this transport (correct for the multi-instance topology).
 *
 * <p>Error routing is identical to the Kafka client and reuses the contract's {@link KafkaErrorCodeMapper} +
 * {@link CoreBankingErrors}: HTTP-style {@code errorCode} 500–599 ⇒ server error (retryable category), 400–499 ⇒ client
 * error (terminal), else business failure; reply timeout ⇒ {@code KAFKA_REPLY_TIMEOUT}; any {@link JMSException} /
 * connection failure ⇒ {@code KAFKA_BROKER_UNAVAILABLE}.
 *
 * <p>The {@link Connection} is opened lazily on first use and cached; a {@link Session} is created per call (cheap, and
 * sidesteps JMS session single-threading). No {@code jakarta.jms} type leaks past this class; this module never
 * references {@code fcb-messaging-kafka}.
 */
public class ArtemisFcbRequestReplyClient implements FcbRequestReplyClient {

    private static final Logger LOG = LoggerFactory.getLogger(ArtemisFcbRequestReplyClient.class);

    // Pinned JMS string-property names — JMS-legal identifiers (no hyphens). Must match the FCB Artemis listener.
    private static final String PROP_OPERATION_TYPE = "operationType";
    private static final String PROP_EVENT_UID = "eventUid";
    private static final String PROP_IDEMPOTENCY_KEY = "idempotencyKey";
    private static final String PROP_AUTHORIZATION = "authorization";
    private static final String PROP_REQUEST_DATETIME = "requestDateTime";
    private static final String PROP_REQUEST_DEADLINE_EPOCH_MS = "requestDeadlineEpochMs";
    private static final String PROP_HOST = "host";
    private static final String PROP_TRACEPARENT = "traceparent";
    private static final String PROP_ACTOR_ENVELOPE = "actorEnvelope";
    private static final String PROP_ACCEPT_LANGUAGE = "acceptLanguage";

    private static final String PRODUCER_CODE = "NOVA";
    private static final String ACCEPT_LANGUAGE_FA = "fa";
    private static final String UNKNOWN_HOST = "unknown";

    /**
     * Bounded retry, mirroring the Kafka client's {@code FcbResilienceConfig} (maxRetries=2 ⇒ 3 attempts). Only
     * genuinely transient <em>transport</em> failures retry: a broker/connection {@link JMSException} and an FCB 5xx
     * ({@code FcbServerException}). A reply timeout is <strong>not</strong> retried — it already consumed the full
     * per-call window, and re-sending would multiply latency on an already-slow downstream (the SLO-eating foot-gun the
     * Kafka module's resilience config warns about; the Kafka client only retries a timeout because it shrinks each
     * attempt to a shared wall-time budget, machinery this lean JMS client deliberately omits). Serialization, client
     * (4xx) and business errors are terminal. The {@code eventUid} is generated once per call so a retried send stays
     * idempotent on the FCB side.
     */
    private static final int MAX_ATTEMPTS = 3;

    private static final long RETRY_BASE_DELAY_MS = 250L;
    private static final long RETRY_MAX_DELAY_MS = 2_000L;

    private static final String METRIC_LATENCY = "fcb.artemis.request_reply.latency";
    private static final String TAG_OPERATION = "operation";
    private static final String TAG_OUTCOME = "outcome";
    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_FAILURE = "failure";

    // APM-dependency span tags. A CLIENT span with peer.service + messaging.system makes the FCB Artemis hop render as
    // a distinct dependency node in Elastic APM (same scheme the Kafka client uses, which is why FCB-over-Kafka,
    // Redis and Postgres already appear). messaging.system=activemq distinguishes the Artemis corridor from Kafka.
    private static final String SPAN_NAME_PREFIX = "fcb-legacy ";
    private static final String PEER_SERVICE = "fcb-legacy";
    private static final String MESSAGING_SYSTEM = "activemq";

    private final ConnectionFactory connectionFactory;
    private final ArtemisFcbProperties properties;
    private final ObjectMapper objectMapper;
    private final ServiceTokenProvider serviceTokenProvider;
    private final AuthenticationContextHolder authenticationContextHolder;
    private final ActorEnvelopeFactory envelopeFactory;
    private final ActorEnvelopeSigner envelopeSigner;
    private final MeterRegistry meterRegistry;
    private final @Nullable Tracer tracer;

    /** Lazily-opened, cached broker connection. Opened on the first send so a down broker never blocks boot. */
    private final AtomicReference<@Nullable Connection> connectionRef = new AtomicReference<>();

    private volatile @Nullable String cachedHost;

    public ArtemisFcbRequestReplyClient(
            ConnectionFactory connectionFactory,
            ArtemisFcbProperties properties,
            ObjectMapper objectMapper,
            ServiceTokenProvider serviceTokenProvider,
            AuthenticationContextHolder authenticationContextHolder,
            ActorEnvelopeFactory envelopeFactory,
            ActorEnvelopeSigner envelopeSigner,
            MeterRegistry meterRegistry,
            @Nullable Tracer tracer) {
        this.connectionFactory = connectionFactory;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.serviceTokenProvider = serviceTokenProvider;
        this.authenticationContextHolder = authenticationContextHolder;
        this.envelopeFactory = envelopeFactory;
        this.envelopeSigner = envelopeSigner;
        this.meterRegistry = meterRegistry;
        this.tracer = tracer;
    }

    @Override
    public Result<FcbKafkaBaseResponse> sendAndReceive(FcbKafkaBaseRequest request, Duration timeout) {
        // eventUid is generated ONCE here and reused as the JMS correlation id + eventUid/idempotencyKey property on
        // EVERY retry attempt, so a resend stays idempotent on the FCB side.
        request.setProducerCode(PRODUCER_CODE);
        request.setEventUid(UUID.randomUUID().toString());
        request.setDateTime(Date.from(ZonedDateTime.now().toInstant()));
        request.setVersion(1);

        String operationType = request.getOperationName();
        long startNanos = System.nanoTime();
        boolean success = false;
        try {
            Result<FcbKafkaBaseResponse> result = sendWithRetry(request, operationType, timeout);
            success = result.isSuccess();
            return result;
        } finally {
            recordLatency(operationType, startNanos, success);
        }
    }

    /**
     * Bounded retry around {@link #doSendAndReceive} mirroring the Kafka client's {@code FcbResilienceConfig}: at most
     * {@link #MAX_ATTEMPTS} attempts, retrying only a transient broker/connection {@link JMSException} or an FCB 5xx
     * ({@link FcbServerException}). A reply timeout ({@link ReplyTimeoutException}), FCB client errors (4xx), business
     * failures and serialization errors are terminal and returned on the first attempt. The {@code eventUid} is stable
     * across attempts (set in the caller), so a retried request is idempotent on the FCB side.
     */
    private Result<FcbKafkaBaseResponse> sendWithRetry(
            FcbKafkaBaseRequest request, String operationType, Duration timeout) {
        Result<FcbKafkaBaseResponse> last = Result.failure(CoreBankingErrors.KAFKA_COMMUNICATION_ERROR, operationType);
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                // Success or a terminal failure Result (client/business error) returns immediately; only the
                // exception cases below are transient and loop.
                return doSendAndReceive(request, operationType, timeout);
            } catch (ReplyTimeoutException e) {
                // Terminal: the attempt already burned the full per-call window; retrying would only multiply latency.
                return Result.failure(
                        CoreBankingErrors.KAFKA_REPLY_TIMEOUT, operationType, String.valueOf(timeout.toMillis()));
            } catch (FcbServerException e) {
                // 5xx from FCB — transient on its side; retry like the Kafka client (FcbServerException is retryable).
                last = Result.failure(CoreBankingErrors.KAFKA_FCB_SERVER_ERROR, e.getErrorCode(), e.getErrorMessage());
            } catch (FcbSerializationException e) {
                return Result.failure(
                        Notification.ofError(CoreBankingErrors.KAFKA_SERIALIZATION_ERROR, e.getErrorMessage()));
            } catch (JMSException e) {
                String message =
                        e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                // A failed connection invalidates the cached one so the next attempt retries a fresh connect.
                resetConnection();
                last = Result.failure(CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE, message);
            } catch (RuntimeException e) {
                String message =
                        e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                return Result.failure(Notification.ofError(CoreBankingErrors.KAFKA_COMMUNICATION_ERROR, message));
            }
            if (attempt < MAX_ATTEMPTS && !backoff(attempt)) {
                break; // interrupted — stop retrying and return the last failure
            }
        }
        return last;
    }

    /** Exponential backoff (250ms → 2s) between attempts. Returns {@code false} if the thread was interrupted. */
    private static boolean backoff(int attempt) {
        long delay = Math.min(RETRY_MAX_DELAY_MS, RETRY_BASE_DELAY_MS << (attempt - 1));
        try {
            Thread.sleep(delay);
            return true;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Opens a per-operation CLIENT span ({@code fcb-legacy <op>}, {@code messaging.system=activemq},
     * {@code peer.service=fcb-legacy}) around the JMS round-trip so the FCB Artemis hop renders as an APM dependency
     * node — mirroring the Kafka client. The traceparent stamped on the outbound JMS message (see
     * {@link #currentTraceparent()}) is read from THIS span, so the FCB hop chains underneath it. A no-op when no
     * {@link Tracer} is wired (test slices).
     */
    private Result<FcbKafkaBaseResponse> doSendAndReceive(
            FcbKafkaBaseRequest request, String operationType, Duration timeout)
            throws JMSException, ReplyTimeoutException {
        if (tracer == null) {
            return doSendAndReceiveInSpan(request, operationType, timeout);
        }
        Span span = tracer.spanBuilder()
                .name(SPAN_NAME_PREFIX + operationType)
                .kind(Span.Kind.CLIENT)
                .tag("peer.service", PEER_SERVICE)
                .tag("messaging.system", MESSAGING_SYSTEM)
                .tag("messaging.operation", "request_reply")
                .tag("messaging.destination.name", properties.getRequestAddress())
                .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            return doSendAndReceiveInSpan(request, operationType, timeout);
        } catch (JMSException | ReplyTimeoutException | RuntimeException e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private Result<FcbKafkaBaseResponse> doSendAndReceiveInSpan(
            FcbKafkaBaseRequest request, String operationType, Duration timeout)
            throws JMSException, ReplyTimeoutException {

        String body = serializeRequest(request);

        // Token mode follows the bound security context (mirrors the Kafka client): an interactive REST call delegates
        // the caller's user token (AUTO); a background path with no bound context (e.g. the recon sweep) uses a
        // client-credentials service token (async), avoiding the FAIL_FAST ambiguous-context throw.
        ServiceTokenRequest tokenRequest =
                authenticationContextHolder.authentication().isPresent()
                        ? ServiceTokenRequest.auto()
                        : ServiceTokenRequest.async();
        OAuth2TokenResponse token = serviceTokenProvider.getServiceToken(tokenRequest);
        String bearerValue = buildBearerHeader(token);
        String signedEnvelope = envelopeSigner.sign(buildEnvelope());

        Connection connection = connection();
        try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            TemporaryQueue replyQueue = session.createTemporaryQueue();
            try (MessageConsumer consumer = session.createConsumer(replyQueue)) {
                Queue requestQueue = session.createQueue(properties.getRequestAddress());
                try (MessageProducer producer = session.createProducer(requestQueue)) {
                    long deadlineMs = System.currentTimeMillis() + timeout.toMillis();
                    TextMessage message = session.createTextMessage(body);
                    message.setJMSReplyTo(replyQueue);
                    message.setJMSCorrelationID(request.getEventUid());
                    stampProperties(message, request, operationType, bearerValue, signedEnvelope, deadlineMs);
                    producer.send(message);

                    Message reply = consumer.receive(timeout.toMillis());
                    if (reply == null) {
                        // Transient: let the bounded retry try again (the eventUid is stable, so a resend is
                        // idempotent on FCB); the terminal KAFKA_REPLY_TIMEOUT failure is produced once retries are
                        // exhausted in sendWithRetry.
                        throw new ReplyTimeoutException();
                    }
                    return handleReply(reply, operationType);
                }
            }
        }
    }

    private Result<FcbKafkaBaseResponse> handleReply(Message reply, String operationType) throws JMSException {
        if (!(reply instanceof TextMessage textReply)) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, operationType);
        }
        String payload = textReply.getText();
        if (payload == null || payload.isBlank()) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, operationType);
        }

        FcbKafkaBaseResponse response;
        try {
            response = objectMapper.readValue(payload, FcbKafkaBaseResponse.class);
        } catch (JacksonException e) {
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

    private void stampProperties(
            TextMessage message,
            FcbKafkaBaseRequest request,
            String operationType,
            String bearerValue,
            String signedEnvelope,
            long deadlineMs)
            throws JMSException {
        message.setStringProperty(PROP_OPERATION_TYPE, operationType);
        message.setStringProperty(PROP_EVENT_UID, request.getEventUid());
        message.setStringProperty(PROP_IDEMPOTENCY_KEY, request.getEventUid());
        message.setStringProperty(PROP_AUTHORIZATION, bearerValue);
        message.setStringProperty(
                PROP_REQUEST_DATETIME, request.getDateTime().toInstant().toString());
        message.setStringProperty(PROP_REQUEST_DEADLINE_EPOCH_MS, Long.toString(deadlineMs));
        message.setStringProperty(PROP_HOST, resolveHost());
        message.setStringProperty(PROP_ACCEPT_LANGUAGE, ACCEPT_LANGUAGE_FA);
        message.setStringProperty(PROP_ACTOR_ENVELOPE, signedEnvelope);
        String traceparent = currentTraceparent();
        if (traceparent != null) {
            message.setStringProperty(PROP_TRACEPARENT, traceparent);
        }
    }

    private String serializeRequest(FcbKafkaBaseRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JacksonException e) {
            throw new FcbSerializationException("Failed to serialize request: " + e.getMessage());
        }
    }

    /**
     * Returns the cached broker {@link Connection}, opening (and starting) it on first use. Synchronised because the
     * JMS connection is shared across calling threads. On any failure the reference is left null so the next call
     * retries the connect.
     */
    private Connection connection() throws JMSException {
        Connection existing = connectionRef.get();
        if (existing != null) {
            return existing;
        }
        synchronized (connectionRef) {
            existing = connectionRef.get();
            if (existing != null) {
                return existing;
            }
            Connection created = openConnection();
            connectionRef.set(created);
            return created;
        }
    }

    private Connection openConnection() throws JMSException {
        String user = properties.getUser();
        Connection connection;
        if (user == null || user.isBlank()) {
            connection = connectionFactory.createConnection();
        } else {
            connection = connectionFactory.createConnection(user, properties.getPassword());
        }
        try {
            connection.start();
        } catch (JMSException e) {
            closeQuietly(connection);
            throw e;
        }
        LOG.info("FCB-ARTEMIS: opened broker connection to {}", properties.getBrokerUrl());
        return connection;
    }

    private void resetConnection() {
        Connection stale = connectionRef.getAndSet(null);
        if (stale != null) {
            closeQuietly(stale);
        }
    }

    private static void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (JMSException ignored) {
            // best-effort close of a connection we are already discarding
        }
    }

    private String buildBearerHeader(@Nullable OAuth2TokenResponse token) {
        if (token == null) {
            throw new IllegalStateException("ServiceTokenProvider returned null token");
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

    /**
     * The W3C {@code traceparent} for the active span, so the FCB hop is correlated in APM (the Kafka client gets this
     * for free from the {@code ReplyingKafkaTemplate}'s Micrometer observation; the JMS client has no equivalent, so we
     * stamp it explicitly — matching what the FCB-side reply path propagates). Built as
     * {@code 00-<traceId>-<spanId>-<flags>} from the current span's {@link TraceContext}. Returns {@code null} when no
     * {@link Tracer} is wired (test slices) or no span is active; FCB tolerates the property's absence.
     */
    private @Nullable String currentTraceparent() {
        if (tracer == null) {
            return null;
        }
        Span span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        TraceContext ctx = span.context();
        String traceId = ctx.traceId();
        String spanId = ctx.spanId();
        if (traceId == null || spanId == null) {
            return null;
        }
        String flags = Boolean.TRUE.equals(ctx.sampled()) ? "01" : "00";
        return "00-" + traceId + "-" + spanId + "-" + flags;
    }

    private String resolveHost() {
        String c = cachedHost;
        if (c != null) {
            return c;
        }
        String resolved = computeHost();
        cachedHost = resolved;
        return resolved;
    }

    private static String computeHost() {
        String env = System.getenv("HOSTNAME");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        env = System.getenv("COMPUTERNAME");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        try {
            String h = InetAddress.getLocalHost().getHostName();
            if (h != null && !h.isBlank()) {
                return h.trim();
            }
        } catch (UnknownHostException ignored) {
            // fall through to JVM name
        }
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            int at = name.indexOf('@');
            if (at >= 0 && at + 1 < name.length()) {
                return name.substring(at + 1);
            }
        } catch (RuntimeException ignored) {
            // fall through to UNKNOWN
        }
        return UNKNOWN_HOST;
    }

    private void recordLatency(String operationType, long startNanos, boolean success) {
        Timer.builder(METRIC_LATENCY)
                .tag(TAG_OPERATION, operationType)
                .tag(TAG_OUTCOME, success ? OUTCOME_SUCCESS : OUTCOME_FAILURE)
                .register(meterRegistry)
                .record(Duration.ofNanos(System.nanoTime() - startNanos));
    }

    /**
     * Internal signal that a reply did not arrive within the per-attempt timeout — retryable in {@link #sendWithRetry}.
     */
    private static final class ReplyTimeoutException extends Exception {
        private ReplyTimeoutException() {
            super(null, null, false, false);
        }
    }
}
