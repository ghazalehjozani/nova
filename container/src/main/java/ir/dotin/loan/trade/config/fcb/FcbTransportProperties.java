package ir.dotin.loan.trade.config.fcb;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Selects the active Nova ↔ FCB request/reply transport ({@code nova.fcb.transport-mode}).
 *
 * <p>A mutable POJO (not a {@code record}) so {@code ConfigurationPropertiesRebinder} can refresh it live on a Consul
 * {@code RefreshEvent} — the router reads {@link #getTransportMode()} per call and picks up the flipped value with no
 * redeploy and no proxy. The code default is {@link TransportMode#KAFKA} so a deploy with the new config absent behaves
 * exactly as today (Kafka-only); "Artemis primary" is realised in {@code nova-config}, not in code.
 */
@Data
@ConfigurationProperties(prefix = FcbTransportProperties.PREFIX)
// why: Error Prone 2.50.0 UnrecognisedJavadocTag is a false positive — it flags valid inline {@code}/{@link} tags in
// @ConfigurationProperties field javadoc under JDK 25; tags are well-formed.
@SuppressWarnings("UnrecognisedJavadocTag")
public class FcbTransportProperties {

    public static final String PREFIX = "nova.fcb";

    /** SAFE DEFAULT = today's behaviour. Flipped to {@link TransportMode#ARTEMIS} via Consul once the broker exists. */
    private TransportMode transportMode = TransportMode.KAFKA;

    /** The interchangeable corridor transports behind the {@code FcbRequestReplyClient} seam. */
    public enum TransportMode {
        KAFKA,
        ARTEMIS
    }
}
