package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.kafka.KafkaException;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbServerException;

/**
 * Retry template for transient Kafka failures.
 *
 * <p>The previous {@code Resilience4j} circuit breaker has been removed and replaced by
 * {@link ir.dotin.loan.trade.adapters.driven.fcbmessaging.health.FcbHealthGate}, which relies on an active heartbeat
 * probe rather than consuming user requests as probes. See the package-level README for rationale.
 *
 * <p>Retry is now only applied to genuinely transient conditions and is intentionally bounded — in a financial system
 * you do not want to burn seconds retrying an already-broken downstream. The gate catches sustained outages almost
 * immediately after the probe classifies FCB as DOWN.
 *
 * <p><strong>Total wall-time bound.</strong> Retry count alone is not enough: retrying a {@code TimeoutException}
 * re-pays the full per-call reply timeout on every attempt (~3x the per-call timeout worst case). Two layers bound the
 * total elapsed time of a request/reply call:
 *
 * <ol>
 *   <li><b>Per-call (primary, adaptive):</b> {@code FcbKafkaClient} computes a wall-time budget per call
 *       ({@code perCallTimeout x retryBudgetMultiplier}, optionally capped by {@code retryMaxElapsed}) and shrinks each
 *       retry's reply timeout to the remaining budget, so retries cannot exceed roughly one extra attempt. This adapts
 *       to the 10s default vs 60s transaction timeouts that the singleton policy below cannot see.
 *   <li><b>Central (backstop):</b> {@link RetryPolicy.Builder#timeout(Duration)} on this singleton policy caps the
 *       maximum elapsed time across the initial invocation and all retries (checked between attempts). It is sized to
 *       the worst-case per-call budget (transaction timeout x multiplier) so it never truncates a legitimate
 *       transaction call, but still backstops the wall-time even if the per-call logic were bypassed.
 * </ol>
 *
 * <p>This is NOT a circuit breaker (forbidden here — {@code FcbHealthGate} owns outage detection); it only bounds how
 * long one call may spend retrying.
 */
@Configuration
@Profile("kafka-fcb")
public class FcbResilienceConfig {

    public static final String FCB_KAFKA_RETRY_TEMPLATE = "fcbKafkaRetryTemplate";

    @Bean(FCB_KAFKA_RETRY_TEMPLATE)
    public RetryTemplate fcbKafkaRetryTemplate(FcbKafkaProperties properties) {
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(2L)
                .delay(Duration.ofMillis(250))
                .multiplier(2.0)
                .maxDelay(Duration.ofSeconds(2))
                .timeout(centralWallTimeCeiling(properties))
                .includes(FcbServerException.class)
                .includes(KafkaException.class)
                .includes(TimeoutException.class)
                .build();
        return new RetryTemplate(retryPolicy);
    }

    /**
     * Absolute wall-time ceiling for the singleton policy. The per-call budget in {@code FcbKafkaClient} is the real
     * bound; this only has to be large enough never to truncate the longest legitimate call (a transaction at
     * {@code transactionTimeout x retryBudgetMultiplier}), then capped by {@code retryMaxElapsed} when set.
     */
    private static Duration centralWallTimeCeiling(FcbKafkaProperties properties) {
        Duration worstCasePerCall = properties.getTransactionTimeout();
        long ceilingNanos = (long) (worstCasePerCall.toNanos() * properties.getRetryBudgetMultiplier());
        Duration cap = properties.getRetryMaxElapsed();
        if (cap != null && !cap.isZero() && !cap.isNegative()) {
            ceilingNanos = Math.min(ceilingNanos, cap.toNanos());
        }
        return Duration.ofNanos(Math.max(ceilingNanos, worstCasePerCall.toNanos()));
    }
}
