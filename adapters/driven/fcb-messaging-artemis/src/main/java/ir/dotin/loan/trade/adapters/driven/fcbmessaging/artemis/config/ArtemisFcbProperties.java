package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = ArtemisFcbProperties.PREFIX)
public class ArtemisFcbProperties {

    public static final String PREFIX = "nova.fcb.artemis";

    private boolean enabled = false;

    private String brokerUrl = "tcp://localhost:61616";

    private String user = "";

    private String password = "";

    private String requestAddress = "nova.fcb.integration.request.v1";

    private String jwksRequestAddress = "nova.fcb.jwks.request.v1";

    private String replyQueuePrefix = "nova.fcb.integration.reply.";

    private String jwksReplyQueuePrefix = "nova.fcb.jwks.reply.";

    /**
     * Explicit per-instance id appended to the reply queue prefix. Blank → resolver falls back to
     * KUBERNETES_POD_NAME → HOSTNAME. MUST be unique per running instance: two instances sharing a
     * reply queue steal each other's replies. When running more than one JVM on the same host,
     * set this explicitly per instance.
     */
    private String instanceId = "";

    /**
     * Max wait for a correlated reply. MUST stay strictly below FCB's reply TTL
     * (ARTEMIS_REPLY_TIMEOUT_MS, default 40s) — see the timeout contract on ArtemisFcbConfig.
     */
    private Duration replyTimeout = Duration.ofSeconds(30);

    /**
     * Period between reply-consumer liveness probes. Push consumers receive no signal when
     * broker-side delivery silently stops (e.g. zombie consumer after a total broker state
     * wipe), so liveness is verified actively by the Pangaea client.
     */
    private Duration livenessCheckInterval = Duration.ofSeconds(5);

    /** Max wait for a single probe echo (also the probe message TTL). */
    private Duration livenessProbeTimeout = Duration.ofSeconds(3);

    /** Consecutive probe failures that trigger a reply session+consumer rebuild. */
    private int livenessFailureThreshold = 2;

    private Duration transactionTimeout = Duration.ofSeconds(60);
}
