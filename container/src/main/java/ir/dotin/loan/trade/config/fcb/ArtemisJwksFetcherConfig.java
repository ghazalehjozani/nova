package ir.dotin.loan.trade.config.fcb;

import jakarta.jms.ConnectionFactory;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import ir.dotin.platform.pangaea.envelope.api.ActorEnvelopeFactory;
import ir.dotin.platform.pangaea.envelope.api.ActorEnvelopeSigner;
import ir.dotin.platform.pangaea.envelope.api.JwksFetcher;
import ir.dotin.platform.pangaea.envelope.impl.jws.config.EnvelopeProperties;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.RedisTrustedKeyStore;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.TrustedKeyStore;
import ir.dotin.platform.pangaea.security.api.ServiceTokenProvider;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config.ArtemisFcbConfig;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config.ArtemisFcbProperties;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import tools.jackson.databind.ObjectMapper;

/**
 * Wires {@link ArtemisJwksFetcher} as the {@link Primary} {@link JwksFetcher} so pangaea's
 * {@code JwsActorEnvelopeVerifier} (which injects {@code ObjectProvider<JwksFetcher>.getIfAvailable()}, honouring
 * {@code @Primary}) resolves the Artemis fetch instead of any Kafka fetcher.
 *
 * <p>JWKS is now an Artemis-only corridor: the {@code replyingKafkaTemplate} bean that used to spawn pangaea's
 * {@code KafkaJwksFetcher} has been removed from {@code FcbKafkaConfig}, so the only {@code JwksFetcher} candidate is
 * this one. Gated on {@code nova.fcb.artemis.enabled} ({@code matchIfMissing=false}, the same master switch as
 * {@link ArtemisFcbConfig}); when Artemis is disabled there is simply no fetcher — exactly like a Kafka-less service,
 * and the verifier degrades to its cached/stale-key path.
 *
 * <p>Like pangaea's {@code KafkaJwksFetcher} the fetch requires the Redis-backed {@link RedisTrustedKeyStore} (the JWK
 * set is written into Redis + the L1 cache). With Artemis explicitly enabled but the trusted-key cache not set to
 * {@code REDIS}, this fails fast at startup rather than silently registering a no-op fetcher.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = ArtemisFcbProperties.PREFIX, name = "enabled")
public class ArtemisJwksFetcherConfig {

    @Bean
    @Primary
    public JwksFetcher artemisJwksFetcher(
            @Qualifier(ArtemisFcbConfig.ARTEMIS_FCB_CONNECTION_FACTORY) ConnectionFactory artemisFcbConnectionFactory,
            ArtemisFcbProperties artemisProperties,
            ObjectMapper objectMapper,
            ServiceTokenProvider serviceTokenProvider,
            ActorEnvelopeFactory envelopeFactory,
            ActorEnvelopeSigner envelopeSigner,
            EnvelopeProperties envelopeProperties,
            TrustedKeyStore trustedKeyStore,
            @Qualifier("envelopeJwksCircuitBreaker") ObjectProvider<CircuitBreaker> circuitBreaker) {
        if (!(trustedKeyStore instanceof RedisTrustedKeyStore redis)) {
            throw new IllegalStateException("JWKS-over-Artemis requires the Redis trusted-key store: set "
                    + "platform.envelope.trusted.cache.backend=REDIS (nova.fcb.artemis.enabled=true is on but "
                    + "the trusted-key store is " + trustedKeyStore.getClass().getSimpleName() + ").");
        }
        CircuitBreaker breaker = circuitBreaker.getIfAvailable();
        return new ArtemisJwksFetcher(
                artemisFcbConnectionFactory,
                artemisProperties,
                objectMapper,
                serviceTokenProvider,
                envelopeFactory,
                envelopeSigner,
                envelopeProperties.getTrusted().getKafka(),
                redis,
                breaker);
    }

    /** Cleanly stops the fetcher's background-refresh executor + cached broker connection on context shutdown. */
    @Bean
    public ArtemisJwksFetcherLifecycle artemisJwksFetcherLifecycle(JwksFetcher artemisJwksFetcher) {
        return new ArtemisJwksFetcherLifecycle(artemisJwksFetcher);
    }

    /** Disposable wrapper so the fetcher's executor/connection are released on shutdown without a public SPI method. */
    public static final class ArtemisJwksFetcherLifecycle implements AutoCloseable {
        private final @Nullable ArtemisJwksFetcher fetcher;

        ArtemisJwksFetcherLifecycle(JwksFetcher fetcher) {
            this.fetcher = (fetcher instanceof ArtemisJwksFetcher a) ? a : null;
        }

        @Override
        public void close() {
            if (fetcher != null) {
                fetcher.shutdown();
            }
        }
    }
}
