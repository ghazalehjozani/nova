package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import org.springframework.context.SmartLifecycle;

/**
 * Drives Spring Boot's readiness state: not "running" until the FCB partition health
 * probe has reported at least one successful cycle. Combined with
 * {@link FcbHealthGate}'s fail-closed startup mode, this prevents requests from being
 * admitted (and the readiness probe from flipping to UP) before the broker is known
 * reachable.
 */
public final class FcbHealthGateLifecycle implements SmartLifecycle {

    private final FcbPartitionHealthRegistry registry;
    private volatile boolean running = false;

    public FcbHealthGateLifecycle(FcbPartitionHealthRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void start() {
        // probe loop drives readiness — nothing to start here
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        if (!running && registry.hasReachedFirstSuccessfulCycle()) {
            running = true;
        }
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
