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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
import jakarta.jms.TemporaryQueue;
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

/**
 * ActiveMQ Artemis (JMS) implementation of pangaea's {@link JwksFetcher} SPI — the actor-envelope trusted-key fetch
 * over the same Artemis corridor Nova uses for FCB request/reply. It is the JMS mirror of pangaea's
 * {@code KafkaJwksFetcher} (same per-kid lock, optional circuit breaker, bounded background-refresh executor, identical
 * {@link RedisTrustedKeyStore} write path), but speaks JMS instead of {@code ReplyingKafkaTemplate}.
 *
 * <p>Wire contract is matched to the FCB-side {@code NovaFcbArtemisJwksListener}:
 *
 * <ul>
 *   <li>request: a {@link TextMessage} sent to {@link ArtemisFcbProperties#getJwksRequestAddress()} whose body is the
 *       JSON of pangaea {@link JwksRequest} ({@code {requestId, requestedKid}} — the field names the listener's
 *       {@code JwksRequest} reads), with {@code JMSReplyTo} = a per-request temporary queue and
 *       {@code JMSCorrelationID} = the request id;
 *   <li>JMS string properties the listener (and the integration client) read: {@code operationType} (the GET_FCB_JWKS
 *       operation name from {@link EnvelopeProperties.Kafka#getOperationType()}), {@code authorization} ({@code "Bearer
 *       " + serviceToken}), {@code actorEnvelope} (signed JWS), {@code eventUid}, {@code idempotencyKey},
 *       {@code requestDateTime}, {@code host}, {@code acceptLanguage};
 *   <li>reply: the listener replies on the temporary queue with the JSON of FCB's {@code JwksReply}; the JWK set rides
 *       as a top-level {@code keys} array of RFC&nbsp;7517 maps, so this fetcher reads {@code keys} (falling back to
 *       the nested pangaea {@code jwks.keys} shape) and feeds each key into the Redis trusted-key store.
 * </ul>
 *
 * <p>The broker {@link Connection} is opened lazily and cached (a down broker never blocks construction); a
 * {@link Session} is created per fetch. Any JMS/timeout/parse failure is logged and returns {@code false} — never
 * throws — so a transient JWKS-fetch outage degrades to "key not yet trusted" rather than failing the verifier.
 */
public final class ArtemisJwksFetcher implements JwksFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(ArtemisJwksFetcher.class);

    // Pinned JMS string-property names — JMS-legal identifiers (no hyphens). Must match the FCB Artemis JWKS listener
    // (authorization + the integration client's stamped set).
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
    private final ThreadPoolExecutor backgroundRefresh = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1),
            new RefreshThreadFactory(),
            new ThreadPoolExecutor.DiscardPolicy());

    /** Lazily-opened, cached broker connection. Opened on the first fetch so a down broker never blocks startup. */
    private final AtomicReference<@Nullable Connection> connectionRef = new AtomicReference<>();

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

    /**
     * Synchronous fetch — used by the verifier when no fresh L1/L2 entry exists. Returns {@code true} if the JWKS was
     * retrieved and the requested kid is now cached. The circuit breaker (if present) gates the underlying JMS
     * request/reply call.
     */
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

    /**
     * Submit an asynchronous refresh for the given kid. Used by the verifier when a stale-but-still-trusted entry is
     * served to the caller. The bounded queue + discard policy prevents refresh storms.
     */
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
        Connection stale = connectionRef.getAndSet(null);
        if (stale != null) {
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

        // JWKS fetch is a background, no-bound-user path; use a client-credentials service token (async) — same choice
        // the integration client makes when no security context is bound.
        OAuth2TokenResponse token = serviceTokenProvider.getServiceToken(ServiceTokenRequest.async());
        String bearer = buildBearerHeader(token);
        String signedEnvelope = envelopeSigner.sign(buildEnvelope());
        String operationType = Objects.requireNonNull(
                jwksConfig.getOperationType(), "platform.envelope.trusted.kafka.operation-type is required");
        long timeoutMs = properties.getReplyTimeout().toMillis();

        try (Connection connection = connection()) {
            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                TemporaryQueue replyQueue = session.createTemporaryQueue();
                try (MessageConsumer consumer = session.createConsumer(replyQueue)) {
                    Queue requestQueue = session.createQueue(properties.getJwksRequestAddress());
                    try (MessageProducer producer = session.createProducer(requestQueue)) {
                        TextMessage message = session.createTextMessage(body);
                        message.setJMSReplyTo(replyQueue);
                        message.setJMSCorrelationID(requestId);
                        message.setStringProperty(PROP_OPERATION_TYPE, operationType);
                        message.setStringProperty(PROP_EVENT_UID, requestId);
                        message.setStringProperty(PROP_IDEMPOTENCY_KEY, requestId);
                        message.setStringProperty(PROP_AUTHORIZATION, bearer);
                        message.setStringProperty(
                                PROP_REQUEST_DATETIME, Instant.now().toString());
                        message.setStringProperty(PROP_HOST, resolveHost());
                        message.setStringProperty(PROP_ACCEPT_LANGUAGE, ACCEPT_LANGUAGE_FA);
                        message.setStringProperty(PROP_ACTOR_ENVELOPE, signedEnvelope);
                        producer.send(message);

                        Message reply = consumer.receive(timeoutMs);
                        if (!(reply instanceof TextMessage textReply)) {
                            return null;
                        }
                        String payload = textReply.getText();
                        if (payload == null || payload.isBlank()) {
                            return null;
                        }
                        return parseReply(payload);
                    }
                }
            }
        }
    }

    /**
     * Parses the FCB JWKS reply. FCB's {@code JwksReply} serialises the JWK set as a top-level {@code keys} array of
     * RFC 7517 maps (not the nested pangaea {@code jwks.keys} shape), so the tree is read directly: {@code keys} is
     * preferred, the nested {@code jwks.keys} is the fallback, and each key map is bound to a pangaea {@link Jwk}. A
     * present {@code errorCode} maps to a failed {@link JwksReply} so {@link JwksReply#isSuccess()} is honoured exactly
     * as on the Kafka path.
     */
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
        if (accessToken.isBlank()) {
            throw new IllegalStateException("OAuth2TokenResponse contains blank accessToken");
        }
        return "Bearer " + accessToken;
    }

    private ActorEnvelope buildEnvelope() {
        // JWKS fetch is a system path with no bound user; sign a config-default system envelope (same as the
        // integration client's no-user branch).
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
