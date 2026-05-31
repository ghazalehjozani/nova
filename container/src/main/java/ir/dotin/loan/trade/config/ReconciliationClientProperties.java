package ir.dotin.loan.trade.config;

import java.time.Duration;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;

/**
 * Nova-side tuning for the facility-state reconciliation adapter, bound from Consul KV under {@code reconciliation.*}.
 *
 * <p>Deliberately a plain mutable class, not a {@code record}: {@code @RefreshScope} proxies via CGLIB and cannot
 * subclass a {@code final} record, so a record would silently break hot reload and serve a stale snapshot for the JVM's
 * lifetime (see container {@code CLAUDE.md}). Any add/rename/remove of a field here requires a matching key in the
 * {@code nova-config} repository.
 *
 * <p>Note: the pangaea sweep cadence / batch sizing lives under {@code platform.reconciliation.sweep.*} (owned by the
 * pangaea starter). These properties are the Nova consumer's own knobs: the per-call FCB recon-state timeout and the
 * page size the {@code FacilityReconciliationSource} requests from the read port.
 */
@Validated
@RefreshScope
@ConfigurationProperties(prefix = "reconciliation")
public class ReconciliationClientProperties {

    /** Per-call timeout for the FCB {@code nova-loanfile-recon-state} request/reply. */
    private Duration reconStateTimeout = Duration.ofSeconds(10);

    /** Page size the facility reconciliation source requests from the read port. */
    @Positive
    private int sourceBatchSize = 200;

    public Duration getReconStateTimeout() {
        return reconStateTimeout;
    }

    public void setReconStateTimeout(Duration reconStateTimeout) {
        this.reconStateTimeout = reconStateTimeout;
    }

    public int getSourceBatchSize() {
        return sourceBatchSize;
    }

    public void setSourceBatchSize(int sourceBatchSize) {
        this.sourceBatchSize = sourceBatchSize;
    }
}
