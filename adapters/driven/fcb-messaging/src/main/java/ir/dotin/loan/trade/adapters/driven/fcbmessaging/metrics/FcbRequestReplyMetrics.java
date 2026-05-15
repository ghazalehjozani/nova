package ir.dotin.loan.trade.adapters.driven.fcbmessaging.metrics;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Observability for Nova → FCB request/reply path.
 *
 * <ul>
 *   <li>{@code fcb.kafka.reply.match.duration{operation,outcome}} — histogram of full send→reply latency.
 *   <li>{@code fcb.kafka.reply.discarded{operation}} — counter incremented when reply arrives past timeout (regression
 *       sentinel for Phase A partition-per-instance routing).
 *   <li>{@code fcb.kafka.publisher.failure{operation,reason}} — counter of producer-level send failures by classified
 *       cause (timeout / broker / server / serialization / other).
 * </ul>
 */
@Component
@Profile("kafka-fcb")
public class FcbRequestReplyMetrics {

    public static final String OUTCOME_SUCCESS = "success";
    public static final String OUTCOME_FAILURE = "failure";

    public static final String REASON_TIMEOUT = "timeout";
    public static final String REASON_BROKER = "broker";
    public static final String REASON_SERVER = "server";
    public static final String REASON_SERIALIZATION = "serialization";
    public static final String REASON_OTHER = "other";

    private static final String TAG_OPERATION = "operation";
    private static final String TAG_OUTCOME = "outcome";
    private static final String TAG_REASON = "reason";

    private static final String METRIC_DURATION = "fcb.kafka.reply.match.duration";
    private static final String METRIC_DISCARDED = "fcb.kafka.reply.discarded";
    private static final String METRIC_FAILURE = "fcb.kafka.publisher.failure";

    private final MeterRegistry registry;

    public FcbRequestReplyMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordMatchDuration(String operation, Duration elapsed, String outcome) {
        Timer.builder(METRIC_DURATION)
                .description("Send→reply correlation latency, tagged by operation and outcome")
                .publishPercentiles(0.5, 0.95, 0.99)
                .tag(TAG_OPERATION, safe(operation))
                .tag(TAG_OUTCOME, safe(outcome))
                .register(registry)
                .record(elapsed.toNanos(), TimeUnit.NANOSECONDS);
    }

    public void recordDiscarded(String operation) {
        Counter.builder(METRIC_DISCARDED)
                .description("Replies discarded due to timeout / no correlation match")
                .tag(TAG_OPERATION, safe(operation))
                .register(registry)
                .increment();
    }

    public void recordPublisherFailure(String operation, String reason) {
        Counter.builder(METRIC_FAILURE)
                .description("Producer-level FCB send failures by classified cause")
                .tag(TAG_OPERATION, safe(operation))
                .tag(TAG_REASON, safe(reason))
                .register(registry)
                .increment();
    }

    private static String safe(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value;
    }
}
