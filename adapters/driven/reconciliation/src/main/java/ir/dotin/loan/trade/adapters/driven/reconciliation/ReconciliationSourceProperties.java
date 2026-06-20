package ir.dotin.loan.trade.adapters.driven.reconciliation;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Nova-side reconciliation tunables. Apply-lost is classified on DURABLE PEER SIGNALS, not a wall clock (LN-59513): the
 * former {@code grace-window}, {@code detection-settle}, and {@code apply-lost-escalate-after} timers are gone. The
 * only surviving time-based escalation is the GENERIC pangaea sweep's {@code divergent-age-threshold}, which escalates
 * a still-divergent row to NEEDS_OPERATOR (human-only) — the liveness backstop for a never-seen gap.
 */
@Data
@ConfigurationProperties(prefix = "reconciliation")
public class ReconciliationSourceProperties {

    /** Upper bound on the page size the facility reconciliation source requests from the read port. */
    private int sourceBatchSize = 200;

    /**
     * Nova-side execution gate for operator-approved REPLAY_FORWARD remediation (dark launch). Even when the management
     * remediation flag admits the request, the replay does not execute unless this is true.
     */
    private boolean replayForwardEnabled = false;

    /**
     * Nova-side gate for guarantor-drift detection and auto-convergence (dark launch). When false (default) the probe
     * never compares guarantor sets (so guarantor drift is never flagged) and the convergence action never republishes
     * a guarantors-changed event. Gates BOTH the probe comparison and the converge branch; leave unset in config to
     * keep the feature off.
     */
    private boolean guarantorDriftEnabled = false;
}
