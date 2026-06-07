package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Toggles whether Nova attaches transaction document metadata (extra info) to outbound FCB documents
 * ({@code nova.fcb.documents.enabled}).
 *
 * <p>A mutable POJO (not a {@code record}) so {@code ConfigurationPropertiesRebinder} can refresh it live on a Consul
 * {@code RefreshEvent} — {@code FcbTransactionAdapter} reads {@link #isEnabled()} per call and picks up the flipped
 * value with no redeploy. The code default is {@code false}: FCB does not yet consume the metadata, so the safe
 * default sends none. It is flipped to {@code true} via {@code nova-config} once FCB supports it.
 */
@Data
@ConfigurationProperties(prefix = "nova.fcb.documents")
public class FcbDocumentProperties {

    /** SAFE DEFAULT = no metadata sent (FCB not ready). Flipped to {@code true} via Consul once FCB consumes it. */
    private boolean enabled = false;
}
