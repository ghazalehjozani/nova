package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class FcbHealthMetrics {

    private final Timer probeLatency;
    private final Counter probeSuccess;
    private final Counter probeFailure;
    private final Counter gateRejections;
    private final Counter transitionsToHealthy;
    private final Counter transitionsToUnhealthy;

    public FcbHealthMetrics(MeterRegistry registry) {
        this.probeLatency = Timer.builder("fcb.kafka.probe.latency")
                .description("FCB Kafka heartbeat probe round-trip latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.probeSuccess = Counter.builder("fcb.kafka.probe.success").register(registry);
        this.probeFailure = Counter.builder("fcb.kafka.probe.failure").register(registry);
        this.gateRejections = Counter.builder("fcb.kafka.gate.rejections").register(registry);
        this.transitionsToHealthy = Counter.builder("fcb.kafka.partition.transitions")
                .tag("to", "healthy")
                .register(registry);
        this.transitionsToUnhealthy = Counter.builder("fcb.kafka.partition.transitions")
                .tag("to", "unhealthy")
                .register(registry);
    }

    public void recordProbeSuccess(long latencyNanos) {
        probeLatency.record(latencyNanos, TimeUnit.NANOSECONDS);
        probeSuccess.increment();
    }

    public void recordProbeFailure() {
        probeFailure.increment();
    }

    public void recordGateRejection() {
        gateRejections.increment();
    }

    public void recordStateTransition(boolean nowHealthy) {
        if (nowHealthy) transitionsToHealthy.increment();
        else transitionsToUnhealthy.increment();
    }
}
