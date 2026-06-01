package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config;

import jakarta.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ir.dotin.platform.pangaea.envelope.api.ActorEnvelopeFactory;
import ir.dotin.platform.pangaea.envelope.api.ActorEnvelopeSigner;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.platform.pangaea.security.api.ServiceTokenProvider;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client.ArtemisFcbRequestReplyClient;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import tools.jackson.databind.ObjectMapper;

/**
 * Wires the ActiveMQ Artemis FCB request/reply transport.
 *
 * <p>Gated by {@code @ConditionalOnProperty(nova.fcb.artemis.enabled, matchIfMissing = false)}: OFF by default, so with
 * the flag absent nothing here instantiates and the {@code artemis-jakarta-client} jar is inert on the classpath
 * (regression-safe). The {@code ConnectionFactory} is constructed eagerly but connects <strong>lazily</strong> —
 * Artemis only opens a socket on the first {@code createConnection()} — so a down/absent broker never blocks Nova boot,
 * even when {@code artemis.enabled=true} but {@code transport-mode=kafka}.
 *
 * <p>The client bean is named {@code artemisFcbRequestReplyClient} so the composition-root router can
 * {@code @Qualifier} it. It is intentionally <strong>not</strong> {@code @Primary}; the router is the only
 * {@code @Primary} {@code FcbRequestReplyClient}.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ArtemisFcbProperties.class)
@ConditionalOnProperty(prefix = ArtemisFcbProperties.PREFIX, name = "enabled", matchIfMissing = false)
public class ArtemisFcbConfig {

    public static final String ARTEMIS_FCB_CLIENT = "artemisFcbRequestReplyClient";
    public static final String ARTEMIS_FCB_CONNECTION_FACTORY = "artemisFcbConnectionFactory";

    /**
     * Artemis JMS {@link ConnectionFactory}. Constructed eagerly from the broker URL + creds, but does <em>not</em>
     * open a connection here — {@code ActiveMQConnectionFactory} connects only when {@code createConnection()} is first
     * invoked (on the first {@code sendAndReceive}), so a missing broker cannot block context startup.
     */
    @Bean(ARTEMIS_FCB_CONNECTION_FACTORY)
    public ConnectionFactory artemisFcbConnectionFactory(ArtemisFcbProperties properties) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(properties.getBrokerUrl());
        // Credentials default onto the factory so the no-arg createConnection() authenticates — Spring Boot's
        // JmsHealthIndicator probes the CF with createConnection() (no user/pass), which on an authenticated Artemis
        // broker fails with AMQ229031 (null username) if the factory carries no creds. The request/reply client still
        // calls createConnection(user, pass) explicitly; setting them here covers the no-arg path identically.
        String user = properties.getUser();
        if (user != null && !user.isBlank()) {
            factory.setUser(user);
            factory.setPassword(properties.getPassword());
        }
        // High-throughput tuning for the request/reply client: non-blocking sends so a request publish does not
        // wait on a broker round-trip (the call still blocks on the reply receive), cached destination lookups,
        // a 1MB reply-consumer prefetch, and infinite reconnect so the corridor self-heals after a broker blip.
        factory.setBlockOnDurableSend(false);
        factory.setBlockOnNonDurableSend(false);
        factory.setCacheDestinations(true);
        factory.setConsumerWindowSize(1024 * 1024);
        factory.setConfirmationWindowSize(1024 * 1024);
        factory.setReconnectAttempts(-1);
        factory.setRetryInterval(2000L);
        factory.setUseGlobalPools(true);
        return factory;
    }

    @Bean(ARTEMIS_FCB_CLIENT)
    public ArtemisFcbRequestReplyClient artemisFcbRequestReplyClient(
            ConnectionFactory artemisFcbConnectionFactory,
            ArtemisFcbProperties properties,
            ObjectMapper objectMapper,
            ServiceTokenProvider serviceTokenProvider,
            AuthenticationContextHolder authenticationContextHolder,
            ActorEnvelopeFactory envelopeFactory,
            ActorEnvelopeSigner envelopeSigner,
            MeterRegistry meterRegistry,
            ObjectProvider<Tracer> tracer) {
        return new ArtemisFcbRequestReplyClient(
                artemisFcbConnectionFactory,
                properties,
                objectMapper,
                serviceTokenProvider,
                authenticationContextHolder,
                envelopeFactory,
                envelopeSigner,
                meterRegistry,
                tracer.getIfAvailable());
    }
}
