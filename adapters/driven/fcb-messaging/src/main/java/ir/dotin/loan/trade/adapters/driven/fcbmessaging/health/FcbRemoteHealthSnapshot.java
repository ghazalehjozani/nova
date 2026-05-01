package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Instant;

import org.jspecify.annotations.Nullable;

/**
 * Immutable snapshot of the legacy (FCB) consumer's self-reported health, as observed via the heartbeat reply.
 *
 * <p>This is informational only. It surfaces the legacy side's view of its own consumer/producer fleet for monitoring,
 * but it does not influence the local readiness gate — that decision is owned by {@link FcbHealthState}.
 */
public record FcbRemoteHealthSnapshot(
        @Nullable String consumerNode,
        @Nullable String healthStatus,
        int totalComponents,
        int upComponents,
        int degradedComponents,
        int downComponents,
        long lastHealthChangeAtEpochMs,
        Instant observedAt) {

    public static FcbRemoteHealthSnapshot empty() {
        return new FcbRemoteHealthSnapshot(null, null, 0, 0, 0, 0, 0L, Instant.EPOCH);
    }

    public boolean isFresh() {
        return observedAt != null && !observedAt.equals(Instant.EPOCH);
    }
}
