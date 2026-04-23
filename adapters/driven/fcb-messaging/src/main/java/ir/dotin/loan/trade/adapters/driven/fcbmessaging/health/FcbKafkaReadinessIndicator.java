package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.util.Objects;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Readiness indicator — exposes the real state of the FCB Kafka integration.
 *
 * <p>Point your Kubernetes {@code readinessProbe} at {@code /actuator/health/readiness}. When FCB is DOWN this endpoint
 * returns {@code DOWN} and Kubernetes removes the pod from Service endpoints, so upstream traffic stops being sent to a
 * pod that cannot fulfil it.
 *
 * <p>DEGRADED maps to {@code OUT_OF_SERVICE} (visible in the endpoint body) but still reports UP at the top level —
 * operators get a warning without losing traffic.
 */
@Component("fcbKafkaReadiness")
@RequiredArgsConstructor
public class FcbKafkaReadinessIndicator extends AbstractHealthIndicator {

    private final FcbHealthState state;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        FcbHealthSnapshot snap = state.snapshot();

        switch (snap.status()) {
            case UP -> builder.up();
            case DEGRADED, RECOVERING -> builder.status("DEGRADED").up();
            case UNKNOWN -> builder.unknown();
            case DOWN -> builder.down();
        }

        builder.withDetail("status", snap.status())
                .withDetail("totalProbes", snap.totalProbes())
                .withDetail("totalSuccesses", snap.totalSuccesses())
                .withDetail("totalFailures", snap.totalFailures())
                .withDetail("consecutiveFailures", snap.consecutiveFailures())
                .withDetail("consecutiveSuccesses", snap.consecutiveSuccesses())
                .withDetail("lastProbeAt", Objects.toString(snap.lastProbeAt(), null))
                .withDetail("lastStatusChangeAt", Objects.toString(snap.lastStatusChangeAt(), null));

        if (snap.lastLatency() != null) {
            builder.withDetail("lastLatencyMs", snap.lastLatency().toMillis());
        }
        if (snap.lastError() != null) {
            builder.withDetail("lastError", snap.lastError());
        }
    }
}
