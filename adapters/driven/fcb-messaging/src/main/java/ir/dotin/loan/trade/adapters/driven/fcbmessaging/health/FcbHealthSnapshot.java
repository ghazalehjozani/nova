package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Duration;
import java.time.Instant;

import org.jspecify.annotations.Nullable;

/**
 * Immutable point-in-time view of the FCB availability state.
 *
 * <p>All fields are final primitives or immutable types — safe to share across threads. A new {@code FcbHealthSnapshot}
 * is produced on every probe completion and swapped into {@link FcbHealthState} atomically.
 */
public record FcbHealthSnapshot(
        FcbHealthStatus status,
        Instant lastProbeAt,
        Instant lastStatusChangeAt,
        long consecutiveSuccesses,
        long consecutiveFailures,
        long totalProbes,
        long totalSuccesses,
        long totalFailures,
        @Nullable Duration lastLatency,
        @Nullable String lastError) {

    public static FcbHealthSnapshot initial(Instant now) {
        return new FcbHealthSnapshot(FcbHealthStatus.UNKNOWN, now, now, 0L, 0L, 0L, 0L, 0L, null, null);
    }

    public FcbHealthSnapshot recordSuccess(Instant now, Duration latency, FcbHealthStatus newStatus) {
        return new FcbHealthSnapshot(
                newStatus,
                now,
                newStatus == this.status ? this.lastStatusChangeAt : now,
                this.consecutiveSuccesses + 1L,
                0L,
                this.totalProbes + 1L,
                this.totalSuccesses + 1L,
                this.totalFailures,
                latency,
                null);
    }

    public FcbHealthSnapshot recordFailure(Instant now, @Nullable String error, FcbHealthStatus newStatus) {
        return new FcbHealthSnapshot(
                newStatus,
                now,
                newStatus == this.status ? this.lastStatusChangeAt : now,
                0L,
                this.consecutiveFailures + 1L,
                this.totalProbes + 1L,
                this.totalSuccesses,
                this.totalFailures + 1L,
                this.lastLatency,
                error);
    }
}
