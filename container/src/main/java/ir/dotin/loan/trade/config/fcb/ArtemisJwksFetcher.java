package ir.dotin.loan.trade.config.fcb;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

import ir.dotin.platform.pangaea.envelope.api.ActorEnvelope;
import ir.dotin.platform.pangaea.envelope.api.ActorEnvelopeFactory;
import ir.dotin.platform.pangaea.envelope.api.ActorEnvelopeSigner;
import ir.dotin.platform.pangaea.envelope.api.ExecutionMode;
import ir.dotin.platform.pangaea.envelope.api.ExecutionTrigger;
import ir.dotin.platform.pangaea.envelope.api.InitiatorType;
import ir.dotin.platform.pangaea.envelope.api.JwksFetcher;
import ir.dotin.platform.pangaea.envelope.impl.jws.config.EnvelopeProperties;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.Jwk;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.JwkSet;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.JwksReply;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.JwksRequest;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.RedisTrustedKeyStore;
import ir.dotin.platform.pangaea.security.api.OAuth2TokenResponse;
import ir.dotin.platform.pangaea.security.api.ServiceTokenProvider;
import ir.dotin.platform.pangaea.security.api.ServiceTokenRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config.ArtemisFcbProperties;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public final class ArtemisJwksFetcher implements JwksFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(ArtemisJwksFetcher.class);

    private static final String PROP_OPERATION_TYPE = "operationType";
    private static final String PROP_EVENT_UID = "eventUid";
    private static final String PROP_IDEMPOTENCY_KEY = "idempotencyKey";
    private static final String PROP_AUTHORIZATION = "authorization";
    private static final String PROP_REQUEST_DATETIME = "requestDateTime";
    private static final String PROP_HOST = "host";
    private static final String PROP_ACTOR_ENVELOPE = "actorEnvelope";
    private static final String PROP_ACCEPT_LANGUAGE = "acceptLanguage";

    private static final String ACCEPT_LANGUAGE_FA = "fa";
    private static final String UNKNOWN_HOST = "unknown";

    private final ConnectionFactory connectionFactory;
    private final ArtemisFcbProperties properties;
    private final ObjectMapper objectMapper;
    private final ServiceTokenProvider serviceTokenProvider;
    private final ActorEnvelopeFactory envelopeFactory;
    private final ActorEnvelopeSigner envelopeSigner;
    private final EnvelopeProperties.Kafka jwksConfig;
    private final RedisTrustedKeyStore trustedKeyStore;
    private final @Nullable CircuitBreaker circuitBreaker;

    private final ConcurrentHashMap<String, Object> kidLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<Message>> pending = new ConcurrentHashMap<>();
    private final Object replyLock = new Object();
    private final ThreadPoolExecutor backgroundRefresh = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1),
            new RefreshThreadFactory(),
            new ThreadPoolExecutor.DiscardPolicy());

    private final AtomicReference<@Nullable Connection> connectionRef = new AtomicReference<>();

    private volatile boolean replyReady = false;
    private volatile @Nullable Session replySession;
    private volatile @Nullable MessageConsumer replyConsumer;
    private volatile @Nullable Queue replyQueue;
    private volatile @Nullable String cachedInstanceId;
    private volatile @Nullable String cachedHost;

    public ArtemisJwksFetcher(
            ConnectionFactory connectionFactory,
            ArtemisFcbProperties properties,
            ObjectMapper objectMapper,
            ServiceTokenProvider serviceTokenProvider,
            ActorEnvelopeFactory envelopeFactory,
            ActorEnvelopeSigner envelopeSigner,
            EnvelopeProperties.Kafka jwksConfig,
            RedisTrustedKeyStore trustedKeyStore,
            @Nullable CircuitBreaker circuitBreaker) {
        this.connectionFactory = connectionFactory;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.serviceTokenProvider = serviceTokenProvider;
        this.envelopeFactory = envelopeFactory;
        this.envelopeSigner = envelopeSigner;
        this.jwksConfig = jwksConfig;
        this.trustedKeyStore = trustedKeyStore;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public boolean fetch(String kid) {
        Object lock = kidLocks.computeIfAbsent(kid, k -> new Object());
        synchronized (lock) {
            try {
                if (trustedKeyStore.get(kid).isPresent()) {
                    return true;
                }
                JwksReply reply = invokeWithBreaker(kid);
                if (reply == null || !reply.isSuccess()) {
                    LOG.warn(
                            "envelope.jwks: artemis fetch returned error kid={} code={} msg={}",
                            kid,
                            reply == null ? "TIMEOUT" : reply.errorCode(),
                            reply == null ? null : reply.errorMessage());
                    return false;
                }
                for (Jwk jwk : Objects.requireNonNull(reply.jwks(), "isSuccess() guarantees jwks is non-null")
                        .keys()) {
                    trustedKeyStore.putJwk(jwk);
                }
                return trustedKeyStore.get(kid).isPresent();
            } catch (CallNotPermittedException cbOpen) {
                LOG.warn("envelope.jwks: circuit breaker open kid={}", kid);
                return false;
            } catch (Exception e) {
                LOG.warn("envelope.jwks: artemis fetch failed kid={} ({})", kid, e.getMessage());
                return false;
            } finally {
                kidLocks.remove(kid);
            }
        }
    }

    @Override
    public void refreshAsync(String kid) {
        try {
            backgroundRefresh.execute(() -> fetch(kid));
        } catch (Exception e) {
            LOG.debug("envelope.jwks: background refresh submit rejected kid={}", kid);
        }
    }

    public void shutdown() {
        backgroundRefresh.shutdownNow();
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

    private @Nullable JwksReply invokeWithBreaker(String kid) throws Exception {
        if (circuitBreaker == null) {
            return doFetch(kid);
        }
        return circuitBreaker.executeCallable(() -> doFetch(kid));
    }

    private @Nullable JwksReply doFetch(String kid) throws Exception {
        String requestId = UUID.randomUUID().toString();
        String body = objectMapper.writeValueAsString(new JwksRequest(requestId, kid));

        OAuth2TokenResponse token = serviceTokenProvider.getServiceToken(ServiceTokenRequest.async());
        String bearer = buildBearerHeader(token);
        String signedEnvelope = envelopeSigner.sign(buildEnvelope());
        String operationType = Objects.requireNonNull(
                jwksConfig.getOperationType(), "platform.envelope.trusted.kafka.operation-type is required");
        long timeoutMs = properties.getReplyTimeout().toMillis();

        ensureReplyInfra();
        Connection connection = connection();
        Queue replyTo = this.replyQueue;
        if (replyTo == null) {
            throw new JMSException("FCB-ARTEMIS jwks reply queue not initialised");
        }

        CompletableFuture<Message> future = new CompletableFuture<>();
        pending.put(requestId, future);
        try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            Queue requestQueue = session.createQueue(properties.getJwksRequestAddress());
            try (MessageProducer producer = session.createProducer(requestQueue)) {
                producer.setTimeToLive(timeoutMs);
                TextMessage message = session.createTextMessage(body);
                message.setJMSReplyTo(replyTo);
                message.setJMSCorrelationID(requestId);
                message.setStringProperty(PROP_OPERATION_TYPE, operationType);
                message.setStringProperty(PROP_EVENT_UID, requestId);
                message.setStringProperty(PROP_IDEMPOTENCY_KEY, requestId);
                message.setStringProperty(PROP_AUTHORIZATION, bearer);
                message.setStringProperty(PROP_REQUEST_DATETIME, Instant.now().toString());
                message.setStringProperty(PROP_HOST, resolveHost());
                message.setStringProperty(PROP_ACCEPT_LANGUAGE, ACCEPT_LANGUAGE_FA);
                message.setStringProperty(PROP_ACTOR_ENVELOPE, signedEnvelope);
                producer.send(message);
            }
            Message reply;
            try {
                reply = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (ExecutionException e) {
                return null;
            }
            if (!(reply instanceof TextMessage textReply)) {
                return null;
            }
            String payload = textReply.getText();
            if (payload == null || payload.isBlank()) {
                return null;
            }
            return parseReply(payload);
        } catch (JMSException e) {
            resetConnection();
            throw e;
        } finally {
            pending.remove(requestId);
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
            LOG.info("envelope.jwks: artemis reply consumer ready on {}", replyQueueName());
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
            LOG.warn("envelope.jwks: failed to read reply correlation id: {}", e.getMessage());
        }
    }

    private JwksReply parseReply(String payload) {
        JsonNode node = objectMapper.readTree(payload);
        String requestId = text(node.get("requestId"));
        String currentKid = text(node.get("currentKid"));
        String errorCode = text(node.get("errorCode"));
        String errorMessage = text(node.get("errorMessage"));
        if (errorCode != null) {
            return new JwksReply(requestId == null ? "" : requestId, null, currentKid, errorCode, errorMessage);
        }
        JsonNode keysNode = node.get("keys");
        if (keysNode == null || keysNode.isNull()) {
            JsonNode jwksNode = node.get("jwks");
            keysNode = jwksNode == null ? null : jwksNode.get("keys");
        }
        if (keysNode == null || !keysNode.isArray()) {
            return new JwksReply(
                    requestId == null ? "" : requestId, null, currentKid, "MALFORMED_REPLY", "missing keys array");
        }
        List<Jwk> keys = new ArrayList<>(keysNode.size());
        for (JsonNode keyNode : keysNode) {
            keys.add(objectMapper.treeToValue(keyNode, Jwk.class));
        }
        return new JwksReply(requestId == null ? "" : requestId, new JwkSet(List.copyOf(keys)), currentKid, null, null);
    }

    private static @Nullable String text(@Nullable JsonNode n) {
        return (n == null || n.isNull()) ? null : n.asString();
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
        if (user.isBlank()) {
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
        LOG.info("envelope.jwks: opened artemis broker connection to {}", properties.getBrokerUrl());
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
        return properties.getJwksReplyQueuePrefix() + instanceId();
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
        if (accessToken.isBlank()) {
            throw new IllegalStateException("OAuth2TokenResponse contains blank accessToken");
        }
        return "Bearer " + accessToken;
    }

    private ActorEnvelope buildEnvelope() {
        return envelopeFactory.fromConfigDefault(
                InitiatorType.SYSTEM_RECOVERY, "system", null, ExecutionTrigger.RECOVERY, ExecutionMode.SYNC);
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

    private static final class RefreshThreadFactory implements ThreadFactory {
        private final AtomicInteger seq = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "envelope-jwks-artemis-refresh-" + seq.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }
}
