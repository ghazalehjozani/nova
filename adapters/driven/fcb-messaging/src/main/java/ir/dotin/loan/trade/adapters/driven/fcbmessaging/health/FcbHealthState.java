package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Single source of truth for FCB Kafka availability. Lock-free. Readers pay one volatile read per access; writers use
 * atomic CAS via {@code updateAndGet}.
 *
 * <p>Only the {@link FcbHealthProbe} writes to this state. The {@link FcbHealthGate} and the Actuator indicators read
 * from it.
 */
@Slf4j
@Component
public class FcbHealthState {

    private final FcbHealthProperties properties;
    private final Clock clock;
    private final AtomicReference<FcbHealthSnapshot> current;

    public FcbHealthState(FcbHealthProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.current = new AtomicReference<>(FcbHealthSnapshot.initial(clock.instant()));
    }

    public FcbHealthSnapshot snapshot() {
        return current.get();
    }

    public FcbHealthStatus status() {
        return current.get().status();
    }

    FcbHealthSnapshot recordProbeSuccess(Duration latency) {
        FcbHealthSnapshot next = current.updateAndGet(prev -> {
            FcbHealthStatus newStatus = computeStatusAfterSuccess(prev, latency);
            return prev.recordSuccess(clock.instant(), latency, newStatus);
        });
        logTransition(next);
        return next;
    }

    FcbHealthSnapshot recordProbeFailure(@Nullable String error) {
        FcbHealthSnapshot next = current.updateAndGet(prev -> {
            FcbHealthStatus newStatus = computeStatusAfterFailure(prev);
            return prev.recordFailure(clock.instant(), error, newStatus);
        });
        logTransition(next);
        return next;
    }

    private FcbHealthStatus computeStatusAfterSuccess(FcbHealthSnapshot prev, Duration latency) {
        boolean overSlo = latency.compareTo(properties.degradedLatencyThreshold()) > 0;
        long nextConsecutiveSuccesses = prev.consecutiveSuccesses() + 1L;

        return switch (prev.status()) {
            case UNKNOWN -> overSlo ? FcbHealthStatus.DEGRADED : FcbHealthStatus.UP;
            case DOWN -> FcbHealthStatus.RECOVERING;
            case RECOVERING -> {
                if (nextConsecutiveSuccesses >= properties.consecutiveSuccessesToClose()) {
                    yield overSlo ? FcbHealthStatus.DEGRADED : FcbHealthStatus.UP;
                }
                yield FcbHealthStatus.RECOVERING;
            }
            case UP, DEGRADED -> overSlo ? FcbHealthStatus.DEGRADED : FcbHealthStatus.UP;
        };
    }

    private FcbHealthStatus computeStatusAfterFailure(FcbHealthSnapshot prev) {
        long nextConsecutiveFailures = prev.consecutiveFailures() + 1L;
        if (nextConsecutiveFailures >= properties.consecutiveFailuresToOpen()) {
            return FcbHealthStatus.DOWN;
        }
        return switch (prev.status()) {
            case DOWN -> FcbHealthStatus.DOWN;
            case RECOVERING -> FcbHealthStatus.DOWN;
            case UNKNOWN, UP, DEGRADED -> prev.status();
        };
    }

    private void logTransition(FcbHealthSnapshot next) {
        if (next.lastStatusChangeAt().equals(next.lastProbeAt())) {
            if (next.status() == FcbHealthStatus.DOWN) {
                log.error(
                        "FCB Kafka health transitioned to DOWN after {} consecutive failures. lastError={}",
                        next.consecutiveFailures(),
                        next.lastError());
            } else if (next.status() == FcbHealthStatus.UP) {
                log.info("FCB Kafka health transitioned to UP.");
            } else {
                log.warn("FCB Kafka health transitioned to {}.", next.status());
            }
        }
    }

    /** Test hook only. Resets state to UNKNOWN — never call from production code. */
    void resetForTesting() {
        current.set(FcbHealthSnapshot.initial(Instant.now(clock)));
    }
}
