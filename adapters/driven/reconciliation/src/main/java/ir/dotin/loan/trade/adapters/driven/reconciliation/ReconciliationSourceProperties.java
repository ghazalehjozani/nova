package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "reconciliation")
public class ReconciliationSourceProperties {

    /** Upper bound on the page size the facility reconciliation source requests from the read port. */
    private int sourceBatchSize = 200;

    /** Detection-settling floor: a facility is not emitted for probing until quiescent at least this long. */
    private Duration detectionSettle = Duration.ofMinutes(3);

    /**
     * Grace window after which an FCB-absent divergence is treated as apply-lost rather than mere FCB lag. A facility
     * whose Nova row last changed less than this long ago is still assumed to be in-flight toward FCB.
     */
    private Duration graceWindow = Duration.ofMinutes(15);

    /**
     * Nova-side execution gate for operator-approved REPLAY_FORWARD remediation (dark launch). Even when the management
     * remediation flag admits the request, the replay does not execute unless this is true.
     */
    private boolean replayForwardEnabled = false;

    /**
     * Fair-chance window for an FCB-absent apply-lost orphan: while the Nova row last changed less than this long ago
     * the action keeps re-driving the stored forward step so the FCB effect-aware re-apply can converge it; once it is
     * exceeded the still-orphan divergence escalates to NEEDS_OPERATOR with its FCB_APPLY_LOST dossier instead of
     * re-driving forever.
     */
    private Duration applyLostEscalateAfter = Duration.ofHours(1);
}
