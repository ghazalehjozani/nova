package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component("fcbKafkaReadiness")
@RequiredArgsConstructor
public class FcbKafkaReadinessIndicator extends AbstractHealthIndicator {

    private final FcbHealthState state;
    private final FcbRemoteHealthState remoteState;

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

        List<FcbRemoteHealthSnapshot> remoteSnapshots = remoteState.allSnapshots();

        if (remoteSnapshots.isEmpty()) {
            builder.withDetail("remote.status", "NOT_OBSERVED");
            return;
        }

        Map<String, Object> nodesDetail = new LinkedHashMap<>();
        int totalRemoteComponents = 0;
        int totalUpComponents = 0;
        int totalDegradedComponents = 0;
        int totalDownComponents = 0;
        boolean anyDown = false;
        boolean anyDegraded = false;

        for (FcbRemoteHealthSnapshot remote : remoteSnapshots) {
            if (!remote.isFresh()) {
                continue;
            }

            String nodeKey = remote.consumerNode() != null ? remote.consumerNode() : "unknown";
            Map<String, Object> nodeDetail = new LinkedHashMap<>();
            nodeDetail.put("status", remote.healthStatus());
            nodeDetail.put("totalComponents", remote.totalComponents());
            nodeDetail.put("upComponents", remote.upComponents());
            nodeDetail.put("degradedComponents", remote.degradedComponents());
            nodeDetail.put("downComponents", remote.downComponents());
            nodeDetail.put("lastHealthChangeAtEpochMs", remote.lastHealthChangeAtEpochMs());
            nodeDetail.put("observedAt", Objects.toString(remote.observedAt(), null));
            nodesDetail.put(nodeKey, nodeDetail);

            totalRemoteComponents += remote.totalComponents();
            totalUpComponents += remote.upComponents();
            totalDegradedComponents += remote.degradedComponents();
            totalDownComponents += remote.downComponents();

            if ("DOWN".equalsIgnoreCase(remote.healthStatus())) {
                anyDown = true;
            } else if ("DEGRADED".equalsIgnoreCase(remote.healthStatus())) {
                anyDegraded = true;
            }
        }

        String aggregateRemoteStatus;
        if (anyDown) {
            aggregateRemoteStatus = "DOWN";
        } else if (anyDegraded) {
            aggregateRemoteStatus = "DEGRADED";
        } else {
            aggregateRemoteStatus = "UP";
        }

        builder.withDetail("remote.status", aggregateRemoteStatus)
                .withDetail("remote.nodeCount", nodesDetail.size())
                .withDetail("remote.totalComponents", totalRemoteComponents)
                .withDetail("remote.upComponents", totalUpComponents)
                .withDetail("remote.degradedComponents", totalDegradedComponents)
                .withDetail("remote.downComponents", totalDownComponents)
                .withDetail("remote.nodes", nodesDetail);
    }
}
