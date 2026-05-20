package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;

class FcbHealthGateModeTest {

    private MeterRegistry meterRegistry;
    private Counter deniedCounter;
    private FcbHealthMetrics metrics;
    private FcbHealthProperties props;
    private FcbPartitionHealthRegistry registry;
    private FcbHealthGate gate;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        deniedCounter = Counter.builder("nova.fcb.health_gate.denied_total").register(meterRegistry);
        metrics = new FcbHealthMetrics(meterRegistry);
        props = new FcbHealthProperties();
        registry = new FcbPartitionHealthRegistry(props, metrics, Clock.systemUTC());
        gate = new FcbHealthGate(registry, props, deniedCounter);
    }

    @Test
    void failClosedDeniesDuringWarmup() {
        props.setStartupMode(FcbHealthProperties.StartupMode.FAIL_CLOSED);

        assertThat(gate.checkPermitted("op").isFailure()).isTrue();
        assertThat(deniedCounter.count()).isEqualTo(1.0);
    }

    @Test
    void failClosedAllowsAfterFirstSuccessfulCycle() {
        props.setStartupMode(FcbHealthProperties.StartupMode.FAIL_CLOSED);

        assertThat(gate.checkPermitted("op").isFailure()).isTrue();

        registry.recordSuccess(0);

        assertThat(registry.hasReachedFirstSuccessfulCycle()).isTrue();
        assertThat(gate.checkPermitted("op").isSuccess()).isTrue();
    }

    @Test
    void failOpenAllowsDuringWarmupWhenNoPartitionsKnown() {
        props.setStartupMode(FcbHealthProperties.StartupMode.FAIL_OPEN);
        props.setFailOpenOnUnknown(true);

        assertThat(gate.checkPermitted("op").isSuccess()).isTrue();
        assertThat(deniedCounter.count()).isEqualTo(0.0);
    }

    @Test
    void healthyPartitionAlwaysAllows() {
        props.setStartupMode(FcbHealthProperties.StartupMode.FAIL_CLOSED);

        registry.recordSuccess(0);

        assertThat(gate.checkPermitted("op").isSuccess()).isTrue();
        assertThat(deniedCounter.count()).isEqualTo(0.0);
    }

    @Test
    void lifecycleReportsRunningOnlyAfterFirstSuccess() {
        FcbHealthGateLifecycle lifecycle = new FcbHealthGateLifecycle(registry);

        assertThat(lifecycle.isRunning()).isFalse();

        registry.recordSuccess(0);

        assertThat(lifecycle.isRunning()).isTrue();
    }
}
