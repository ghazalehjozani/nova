package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config;

import jakarta.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ir.dotin.platform.pangaea.messaging.requestreply.jms.RequestReplyClientFactory;
import ir.dotin.platform.pangaea.messaging.requestreply.jms.RequestReplyConfig;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client.ArtemisFcbRequestReplyClient;

import io.micrometer.core.instrument.MeterRegistry;
import tools.jackson.databind.ObjectMapper;

/**
 * Wires the ActiveMQ Artemis FCB request/reply transport.
 *
 * <p>HA/failover/reconnect parameters live ONLY in the Consul-managed broker URL — no
 * programmatic setters for them here, because setters override URL parameters and silently
 * defeat operational changes. Required URL shape (see Consul fcb.yml):
 * <pre>
 * (tcp://host1:61616,tcp://host2:61616,tcp://host3:61616)
 * ?ha=true&failoverOnInitialConnection=true
 * &initialConnectAttempts=3&reconnectAttempts=-1&failoverAttempts=-1
 * &retryInterval=1000&retryIntervalMultiplier=1.0&maxRetryInterval=2000
 * </pre>
 *
 * <p>Timeout contract (must hold across BOTH ends):
 * {@code replyTimeout} (how long Nova waits for a reply) &lt; FCB reply TTL
 * ({@code ARTEMIS_REPLY_TIMEOUT_MS} on the FCB side). If the broker expires replies earlier
 * than Nova stops waiting, a slow-but-valid reply vanishes into ExpiryQueue and Nova reports a
 * timeout that never should have happened.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ArtemisFcbProperties.class)
@ConditionalOnProperty(prefix = ArtemisFcbProperties.PREFIX, name = "enabled", matchIfMissing = false)
public class ArtemisFcbConfig {

    public static final String ARTEMIS_FCB_CLIENT = "artemisFcbRequestReplyClient";
    public static final String ARTEMIS_FCB_CONNECTION_FACTORY = "artemisFcbConnectionFactory";

    @Bean(name = ARTEMIS_FCB_CONNECTION_FACTORY, destroyMethod = "close")
    public ConnectionFactory artemisFcbConnectionFactory(ArtemisFcbProperties properties) {
        ActiveMQConnectionFactory factory =
                new ActiveMQConnectionFactory(properties.getBrokerUrl());

        String user = properties.getUser();
        if (user != null && !user.isBlank()) {
            factory.setUser(user);
            factory.setPassword(properties.getPassword());
        }

        // ── send behavior (not expressible in URL) ──────────────────────────
        factory.setBlockOnDurableSend(true);        // banking: acknowledge persisted sends
        factory.setBlockOnNonDurableSend(true);     // request/reply is sync anyway; backpressure beats silent loss
        factory.setCacheDestinations(true);

        // ── windows ─────────────────────────────────────────────────────────
        factory.setConfirmationWindowSize(1024 * 1024);
        factory.setConsumerWindowSize(1024 * 1024); // prefetch for the long-lived named reply-queue consumer

        // ── failure detection (not expressible in URL) ──────────────────────
        // Must tolerate a full live→backup failover without the broker reaping the connection.
        factory.setClientFailureCheckPeriod(10_000L);
        factory.setConnectionTTL(60_000L);

        // ── blocking-call bounds ────────────────────────────────────────────
        // Must exceed the worst-case blocking send; below the reply timeout is fine.
        factory.setCallTimeout(30_000L);
        factory.setCallFailoverTimeout(30_000L);

        // ── topology / pooling ──────────────────────────────────────────────
        factory.setUseTopologyForLoadBalancing(true);
        factory.setUseGlobalPools(true);

        return factory;
    }

    @Bean(ARTEMIS_FCB_CLIENT)
    public ArtemisFcbRequestReplyClient artemisFcbRequestReplyClient(
            RequestReplyClientFactory factory,
            @Qualifier(ARTEMIS_FCB_CONNECTION_FACTORY) ConnectionFactory connectionFactory,
            ArtemisFcbProperties properties,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry) {
        // maxAttempts = 1: no transparent transport retry of financial operations. The caller
        // decides whether re-issuing an operation is safe; the idempotency key is stamped per
        // sendAndReceive call, so a transport-level replay could double-execute otherwise.
        RequestReplyConfig config = new RequestReplyConfig(
                properties.getReplyQueuePrefix(),
                properties.getInstanceId(),
                properties.getReplyTimeout(),
                1,
                250L,
                1000L,
                "fcb-legacy",
                "activemq",
                "fcb.artemis.transport.request_reply.latency",
                properties.getLivenessCheckInterval(),
                properties.getLivenessProbeTimeout(),
                properties.getLivenessFailureThreshold());

        return new ArtemisFcbRequestReplyClient(
                factory.create(connectionFactory, config),
                objectMapper,
                properties,
                meterRegistry);
    }
}
