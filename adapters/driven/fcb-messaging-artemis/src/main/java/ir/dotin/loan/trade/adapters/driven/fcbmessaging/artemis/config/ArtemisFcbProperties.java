package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Binding for the ActiveMQ Artemis FCB transport ({@code nova.fcb.artemis.*}).
 *
 * <p>A mutable POJO (not a {@code record}) so the {@code ConfigurationPropertiesRebinder} can refresh it live on a
 * Consul {@code RefreshEvent} — a {@code record} would be re-instantiated as a stale snapshot under a
 * {@code @RefreshScope} proxy. Defaults are the regression-safe values from the pinned contract: the transport is
 * {@link #enabled} OFF by default so nothing Artemis instantiates unless explicitly switched on in {@code nova-config},
 * and the request / JWKS-request addresses match the pinned wire queues shared with FCB.
 */
@Data
@ConfigurationProperties(prefix = ArtemisFcbProperties.PREFIX)
public class ArtemisFcbProperties {

    public static final String PREFIX = "nova.fcb.artemis";

    /**
     * Master switch for the Artemis transport beans. OFF by default: with the flag absent, the
     * {@code @ConditionalOnProperty(matchIfMissing = false)} on {@code ArtemisFcbConfig} keeps the
     * {@code ConnectionFactory}, client, and any listener from instantiating, so the {@code artemis-jakarta-client} jar
     * sits inert on the classpath.
     */
    private boolean enabled = false;

    /**
     * Artemis broker URL (e.g. {@code tcp://localhost:61616}). The {@code ActiveMQConnectionFactory} is constructed
     * from this eagerly but only opens a socket on the first {@code createConnection()} call, so a down broker never
     * blocks boot.
     */
    private String brokerUrl = "tcp://localhost:61616";

    /** Broker username; null/blank ⇒ anonymous connection. */
    private String user = "";

    /** Broker password; paired with {@link #user}. Injected from an env-var placeholder in {@code nova-config}. */
    private String password = "";

    /**
     * Anycast request address/queue the FCB integration listener consumes from. Must match the FCB side byte-for-byte.
     */
    private String requestAddress = "nova.fcb.integration.request.v1";

    /** Anycast JWKS-request address/queue (JWKS fetch is wired separately; kept here for one source of truth). */
    private String jwksRequestAddress = "nova.fcb.jwks.request.v1";

    /** Per-call wall-time budget to await a reply on the temporary reply queue. */
    private Duration replyTimeout = Duration.ofSeconds(10);

    /** Upper bound for transactional / long operations (e.g. issue-general-document). */
    private Duration transactionTimeout = Duration.ofSeconds(60);
}
