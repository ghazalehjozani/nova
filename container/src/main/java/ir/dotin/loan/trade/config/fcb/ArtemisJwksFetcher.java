package ir.dotin.loan.trade.config.fcb;

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

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ir.dotin.platform.pangaea.envelope.api.JwksFetcher;
import ir.dotin.platform.pangaea.envelope.impl.jws.config.EnvelopeProperties;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.Jwk;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.JwkSet;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.JwksReply;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.JwksRequest;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.RedisTrustedKeyStore;
import ir.dotin.platform.pangaea.messaging.requestreply.api.Reply;
import ir.dotin.platform.pangaea.messaging.requestreply.api.ReplyTransportException;
import ir.dotin.platform.pangaea.messaging.requestreply.api.RequestReplyClient;
import ir.dotin.platform.pangaea.messaging.requestreply.api.RequestReplyTimeoutException;
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

    private final RequestReplyClient client;
    private final ArtemisFcbProperties properties;
    private final ObjectMapper objectMapper;
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

    public ArtemisJwksFetcher(
            RequestReplyClient client,
            ArtemisFcbProperties properties,
            ObjectMapper objectMapper,
            EnvelopeProperties.Kafka jwksConfig,
            RedisTrustedKeyStore trustedKeyStore,
            @Nullable CircuitBreaker circuitBreaker) {
        this.client = client;
        this.properties = properties;
        this.objectMapper = objectMapper;
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
    }

    private @Nullable JwksReply invokeWithBreaker(String kid) throws Exception {
        if (circuitBreaker == null) {
            return doFetch(kid);
        }
        return circuitBreaker.executeCallable(() -> doFetch(kid));
    }

    private @Nullable JwksReply doFetch(String kid) throws Exception {
        String requestId = UUID.randomUUID().toString();
        byte[] body = objectMapper.writeValueAsBytes(new JwksRequest(requestId, kid));
        String operationType = Objects.requireNonNull(
                jwksConfig.getOperationType(), "platform.envelope.trusted.kafka.operation-type is required");
        try {
            Reply reply = client.destination(properties.getJwksRequestAddress())
                    .header(PROP_OPERATION_TYPE, operationType)
                    .header(PROP_EVENT_UID, requestId)
                    .header(PROP_IDEMPOTENCY_KEY, requestId)
                    .timeout(properties.getReplyTimeout())
                    .exchange(body);
            String payload = reply.payloadAsString();
            if (payload.isBlank()) {
                return null;
            }
            return parseReply(payload);
        } catch (RequestReplyTimeoutException | ReplyTransportException e) {
            LOG.warn("envelope.jwks: artemis fetch transport failure kid={} ({})", kid, e.getMessage());
            return null;
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
