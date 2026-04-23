package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Liveness indicator — reports UP as long as the probe thread itself is alive and capable of producing snapshots.
 * Critically this does NOT flip to DOWN just because FCB is unreachable, because Kubernetes would then restart the pod
 * while the real problem is a downstream dependency.
 *
 * <p>For "should I take traffic" semantics, use the readiness indicator instead.
 */
@Component("fcbKafkaLiveness")
@RequiredArgsConstructor
public class FcbKafkaLivenessIndicator extends AbstractHealthIndicator {

    private final FcbHealthState state;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        FcbHealthSnapshot snap = state.snapshot();
        builder.up()
                .withDetail("status", snap.status())
                .withDetail("totalProbes", snap.totalProbes())
                .withDetail("lastProbeAt", snap.lastProbeAt());
    }
}
