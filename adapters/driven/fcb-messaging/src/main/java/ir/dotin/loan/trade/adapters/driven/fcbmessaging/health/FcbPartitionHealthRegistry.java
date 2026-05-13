package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcbPartitionHealthRegistry {

    public enum HealthState {
        HEALTHY,
        UNHEALTHY
    }

    public record PartitionState(HealthState state, long history, int sampleCount, long lastTransitionMs) {

        int effectiveWindow(int windowSize) {
            return Math.min(sampleCount, windowSize);
        }

        int successesInWindow(int windowSize) {
            long mask = windowSize >= 64 ? -1L : (1L << windowSize) - 1L;
            return Long.bitCount(history & mask);
        }

        int failuresInWindow(int windowSize) {
            return effectiveWindow(windowSize) - successesInWindow(windowSize);
        }
    }

    private final FcbHealthProperties properties;
    private final FcbHealthMetrics metrics;
    private final Clock clock;

    private final ConcurrentMap<Integer, PartitionState> partitions = new ConcurrentHashMap<>();
    private volatile Set<Integer> healthySet = Set.of();
    private volatile List<Integer> healthyOrdered = List.of();

    public void recordSuccess(int partition) {
        applyOutcome(partition, true);
    }

    public void recordFailure(int partition) {
        applyOutcome(partition, false);
    }

    private void applyOutcome(int partition, boolean success) {
        long now = clock.millis();
        int windowSize = properties.getProbeWindowSize();
        int failuresToOpen = properties.getFailuresInWindowToOpen();
        int successesToClose = properties.getSuccessesInWindowToClose();

        var updated = partitions.compute(partition, (k, prev) -> {
            HealthState prevState = prev == null ? HealthState.UNHEALTHY : prev.state();
            long prevHistory = prev == null ? 0L : prev.history();
            int prevCount = prev == null ? 0 : prev.sampleCount();
            long prevTransition = prev == null ? now : prev.lastTransitionMs();

            long mask = windowSize >= 64 ? -1L : (1L << windowSize) - 1L;
            long newHistory = ((prevHistory << 1) | (success ? 1L : 0L)) & mask;
            int newCount = Math.min(prevCount + 1, windowSize);

            var probed = new PartitionState(prevState, newHistory, newCount, prevTransition);

            if (prevState == HealthState.HEALTHY) {
                if (probed.failuresInWindow(windowSize) >= failuresToOpen) {
                    return new PartitionState(HealthState.UNHEALTHY, newHistory, newCount, now);
                }
                return probed;
            }
            if (probed.successesInWindow(windowSize) >= successesToClose) {
                return new PartitionState(HealthState.HEALTHY, newHistory, newCount, now);
            }
            return probed;
        });

        boolean nowHealthy = updated.state() == HealthState.HEALTHY;
        boolean inCache = healthySet.contains(partition);
        if (nowHealthy != inCache) {
            rebuildCache();
            metrics.recordStateTransition(nowHealthy);
            log.info(
                    "FCB-HEALTH: partition={} -> {} (succ={}/{} fail={}/{})",
                    partition,
                    updated.state(),
                    updated.successesInWindow(windowSize),
                    updated.effectiveWindow(windowSize),
                    updated.failuresInWindow(windowSize),
                    updated.effectiveWindow(windowSize));
        }
    }

    public void markAllUnhealthy() {
        long now = clock.millis();
        partitions.replaceAll((k, v) -> new PartitionState(HealthState.UNHEALTHY, 0L, 0, now));
        rebuildCache();
        log.warn("FCB-HEALTH: all partitions forced UNHEALTHY by watchdog (history cleared)");
    }

    public List<Integer> getHealthyPartitions() {
        return healthyOrdered;
    }

    public int healthyCount() {
        return healthyOrdered.size();
    }

    public int totalKnown() {
        return partitions.size();
    }

    public boolean anyPartitionStillWarming() {
        int needed = properties.getSuccessesInWindowToClose();
        for (PartitionState s : partitions.values()) {
            if (s.sampleCount() < needed) return true;
        }
        return false;
    }

    private synchronized void rebuildCache() {
        List<Integer> healthy = new ArrayList<>();
        for (Map.Entry<Integer, PartitionState> e : partitions.entrySet()) {
            if (e.getValue().state() == HealthState.HEALTHY) {
                healthy.add(e.getKey());
            }
        }
        Collections.sort(healthy);
        this.healthyOrdered = List.copyOf(healthy);
        this.healthySet = Set.copyOf(new HashSet<>(healthy));
    }
}
