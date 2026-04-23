package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Micrometer instruments for the health subsystem. All names are prefixed with {@code fcb.kafka.} so they can be carved
 * out in Grafana dashboards and Prometheus alert rules.
 *
 * <p>Note: we deliberately DO NOT compute a histogram in-process of the gate hot path — the gate call is
 * sub-microsecond and an extra {@code Timer.record} would dominate the cost. Only probe latency is histogrammed.
 */
@Component
public class FcbHealthMetrics {

    private final Timer probeLatency;
    private final Counter probeSuccess;
    private final Counter probeFailure;
    private final Counter gateRejections;
    private final AtomicInteger statusOrdinalGauge = new AtomicInteger(FcbHealthStatus.UNKNOWN.ordinal());

    public FcbHealthMetrics(MeterRegistry registry, FcbHealthState state) {
        this.probeLatency = Timer.builder("fcb.kafka.probe.latency")
                .description("FCB Kafka heartbeat probe round-trip latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.probeSuccess = Counter.builder("fcb.kafka.probe.success")
                .description("FCB Kafka heartbeat probes that succeeded")
                .register(registry);

        this.probeFailure = Counter.builder("fcb.kafka.probe.failure")
                .description("FCB Kafka heartbeat probes that failed")
                .register(registry);

        this.gateRejections = Counter.builder("fcb.kafka.gate.rejections")
                .description("User requests rejected by the health gate without dispatching to Kafka")
                .register(registry);

        registry.gauge("fcb.kafka.health.status", state, s -> s.status().ordinal());
        registry.gauge("fcb.kafka.health.consecutive_failures", state, s ->
                (double) s.snapshot().consecutiveFailures());
        registry.gauge("fcb.kafka.health.consecutive_successes", state, s ->
                (double) s.snapshot().consecutiveSuccesses());
    }

    void recordProbeSuccess(long latencyNanos) {
        probeLatency.record(latencyNanos, TimeUnit.NANOSECONDS);
        probeSuccess.increment();
    }

    void recordProbeFailure() {
        probeFailure.increment();
    }

    public void recordGateRejection() {
        gateRejections.increment();
    }
}
