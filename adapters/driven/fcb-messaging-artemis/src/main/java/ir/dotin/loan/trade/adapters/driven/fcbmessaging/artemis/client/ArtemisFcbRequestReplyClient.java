package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.annotation.PreDestroy;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
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
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbSerializationException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbServerException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.FcbErrorCodeMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

public class ArtemisFcbRequestReplyClient implements FcbRequestReplyClient {

    private static final Logger LOG = LoggerFactory.getLogger(ArtemisFcbRequestReplyClient.class);

    private static final String PROP_OPERATION_TYPE = "operationType";
    private static final String PROP_IDEMPOTENCY_KEY = "idempotencyKey";
    private static final String PROP_AUTHORIZATION = "authorization";
    private static final String PROP_REQUEST_DATETIME = "requestDateTime";
    private static final String PROP_HOST = "host";
    private static final String PROP_TRACEPARENT = "traceparent";
    private static final String PROP_ACTOR_ENVELOPE = "actorEnvelope";
    private static final String PROP_ACCEPT_LANGUAGE = "acceptLanguage";

    private static final String ACCEPT_LANGUAGE_FA = "fa";
    private static final String UNKNOWN_HOST = "unknown";

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BASE_DELAY_MS = 250L;
    private static final long RETRY_MAX_DELAY_MS = 2_000L;

    private static final String METRIC_LATENCY = "fcb.artemis.request_reply.latency";
    private static final String TAG_OPERATION = "operation";
    private static final String TAG_OUTCOME = "outcome";
    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_FAILURE = "failure";

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

    private final AtomicReference<@Nullable Connection> connectionRef = new AtomicReference<>();
    private final ConcurrentHashMap<String, CompletableFuture<Message>> pending = new ConcurrentHashMap<>();
    private final Object replyLock = new Object();

    private volatile boolean replyReady = false;
    private volatile @Nullable Session replySession;
    private volatile @Nullable MessageConsumer replyConsumer;
    private volatile @Nullable Queue replyQueue;
    private volatile @Nullable String cachedInstanceId;
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
    public Result<FcbBaseResponse> sendAndReceive(FcbBaseRequest request, Duration timeout) {
        String operationType = request.getOperationName();
        String idempotencyKey = UUID.randomUUID().toString();
        long startNanos = System.nanoTime();
        boolean success = false;
        try {
            Result<FcbBaseResponse> result = sendWithRetry(request, operationType, idempotencyKey, timeout);
            success = result.isSuccess();
            return result;
        } finally {
            recordLatency(operationType, startNanos, success);
        }
    }

    private Result<FcbBaseResponse> sendWithRetry(
            FcbBaseRequest request, String operationType, String idempotencyKey, Duration timeout) {
        Result<FcbBaseResponse> last = Result.failure(CoreBankingErrors.FCB_COMMUNICATION_ERROR, operationType);
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return doSendAndReceive(request, operationType, idempotencyKey, timeout);
            } catch (ReplyTimeoutException e) {
                return Result.failure(
                        CoreBankingErrors.FCB_REPLY_TIMEOUT, operationType, String.valueOf(timeout.toMillis()));
            } catch (FcbServerException e) {
                last = Result.failure(CoreBankingErrors.FCB_SERVER_ERROR, e.getErrorCode(), e.getErrorMessage());
            } catch (FcbSerializationException e) {
                return Result.failure(
                        Notification.ofError(CoreBankingErrors.FCB_SERIALIZATION_ERROR, e.getErrorMessage()));
            } catch (JMSException e) {
                String message =
                        e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                resetConnection();
                last = Result.failure(CoreBankingErrors.FCB_BROKER_UNAVAILABLE, message);
            } catch (RuntimeException e) {
                String message =
                        e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                return Result.failure(Notification.ofError(CoreBankingErrors.FCB_COMMUNICATION_ERROR, message));
            }
            if (attempt < MAX_ATTEMPTS && !backoff(attempt)) {
                break;
            }
        }
        return last;
    }

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

    private Result<FcbBaseResponse> doSendAndReceive(
            FcbBaseRequest request, String operationType, String idempotencyKey, Duration timeout)
            throws JMSException, ReplyTimeoutException {
        if (tracer == null) {
            return doSendAndReceiveInSpan(request, operationType, idempotencyKey, timeout);
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
            return doSendAndReceiveInSpan(request, operationType, idempotencyKey, timeout);
        } catch (JMSException | ReplyTimeoutException | RuntimeException e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private Result<FcbBaseResponse> doSendAndReceiveInSpan(
            FcbBaseRequest request, String operationType, String idempotencyKey, Duration timeout)
            throws JMSException, ReplyTimeoutException {

        String body = serializeRequest(request);

        ServiceTokenRequest tokenRequest =
                authenticationContextHolder.authentication().isPresent()
                        ? ServiceTokenRequest.auto()
                        : ServiceTokenRequest.async();
        OAuth2TokenResponse token = serviceTokenProvider.getServiceToken(tokenRequest);
        String bearerValue = buildBearerHeader(token);
        String signedEnvelope = envelopeSigner.sign(buildEnvelope());

        ensureReplyInfra();
        Connection connection = connection();
        Queue replyTo = this.replyQueue;
        if (replyTo == null) {
            throw new JMSException("FCB-ARTEMIS reply queue not initialised");
        }

        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<Message> future = new CompletableFuture<>();
        pending.put(correlationId, future);
        try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            Queue requestQueue = session.createQueue(properties.getRequestAddress());
            try (MessageProducer producer = session.createProducer(requestQueue)) {
                producer.setTimeToLive(timeout.toMillis());
                TextMessage message = session.createTextMessage(body);
                message.setJMSReplyTo(replyTo);
                message.setJMSCorrelationID(correlationId);
                stampProperties(message, operationType, idempotencyKey, bearerValue, signedEnvelope);
                producer.send(message);
            }
            Message reply;
            try {
                reply = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw new ReplyTimeoutException();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ReplyTimeoutException();
            } catch (ExecutionException e) {
                throw new ReplyTimeoutException();
            }
            return handleReply(reply, operationType);
        } finally {
            pending.remove(correlationId);
        }
    }

    private void ensureReplyInfra() throws JMSException {
        if (replyReady) {
            return;
        }
        synchronized (replyLock) {
            if (replyReady) {
                return;
            }
            Connection conn = connection();
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(replyQueueName());
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(this::onReply);
            this.replySession = session;
            this.replyConsumer = consumer;
            this.replyQueue = queue;
            this.replyReady = true;
            LOG.info("FCB-ARTEMIS: reply consumer ready on {}", replyQueueName());
        }
    }

    private void onReply(Message message) {
        try {
            String correlationId = message.getJMSCorrelationID();
            if (correlationId == null) {
                return;
            }
            CompletableFuture<Message> future = pending.remove(correlationId);
            if (future != null) {
                future.complete(message);
            }
        } catch (JMSException e) {
            LOG.warn("FCB-ARTEMIS: failed to read reply correlation id: {}", e.getMessage());
        }
    }

    private Result<FcbBaseResponse> handleReply(Message reply, String operationType) throws JMSException {
        if (!(reply instanceof TextMessage textReply)) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, operationType);
        }
        String payload = textReply.getText();
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

    private void stampProperties(
            TextMessage message, String operationType, String idempotencyKey, String bearerValue, String signedEnvelope)
            throws JMSException {
        message.setStringProperty(PROP_OPERATION_TYPE, operationType);
        message.setStringProperty(PROP_IDEMPOTENCY_KEY, idempotencyKey);
        message.setStringProperty(PROP_AUTHORIZATION, bearerValue);
        message.setStringProperty(PROP_REQUEST_DATETIME, Instant.now().toString());
        message.setStringProperty(PROP_HOST, resolveHost());
        message.setStringProperty(PROP_ACCEPT_LANGUAGE, ACCEPT_LANGUAGE_FA);
        message.setStringProperty(PROP_ACTOR_ENVELOPE, signedEnvelope);
        String traceparent = currentTraceparent();
        if (traceparent != null) {
            message.setStringProperty(PROP_TRACEPARENT, traceparent);
        }
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

    private String serializeRequest(FcbBaseRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JacksonException e) {
            throw new FcbSerializationException("Failed to serialize request: " + e.getMessage());
        }
    }

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
        synchronized (replyLock) {
            replyReady = false;
            closeQuietly(replyConsumer);
            closeQuietly(replySession);
            replyConsumer = null;
            replySession = null;
            replyQueue = null;
            Connection stale = connectionRef.getAndSet(null);
            closeQuietly(stale);
        }
    }

    @PreDestroy
    void shutdown() {
        synchronized (replyLock) {
            replyReady = false;
            closeQuietly(replyConsumer);
            closeQuietly(replySession);
            replyConsumer = null;
            replySession = null;
            replyQueue = null;
            Connection conn = connectionRef.getAndSet(null);
            closeQuietly(conn);
        }
    }

    private static void closeQuietly(@Nullable AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception ignored) {
        }
    }

    private static void closeQuietly(@Nullable Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (JMSException ignored) {
        }
    }

    private String replyQueueName() {
        return properties.getReplyQueuePrefix() + instanceId();
    }

    private String instanceId() {
        String c = cachedInstanceId;
        if (c != null) {
            return c;
        }
        String resolved = computeInstanceId();
        cachedInstanceId = resolved;
        return resolved;
    }

    private String computeInstanceId() {
        String configured = properties.getInstanceId();
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        String pod = System.getenv("KUBERNETES_POD_NAME");
        if (pod != null && !pod.isBlank()) {
            return pod.trim();
        }
        String host = computeHost();
        if (!UNKNOWN_HOST.equals(host)) {
            return host;
        }
        return UUID.randomUUID().toString();
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
        }
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            int at = name.indexOf('@');
            if (at >= 0 && at + 1 < name.length()) {
                return name.substring(at + 1);
            }
        } catch (RuntimeException ignored) {
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

    private static final class ReplyTimeoutException extends Exception {
        private ReplyTimeoutException() {
            super(null, null, false, false);
        }
    }
}
