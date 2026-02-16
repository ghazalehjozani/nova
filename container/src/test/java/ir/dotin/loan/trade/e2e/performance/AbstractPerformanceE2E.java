package ir.dotin.loan.trade.e2e.performance;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ir.dotin.loan.trade.e2e.AbstractMessagingE2E;

@Tag("performance")
public abstract class AbstractPerformanceE2E extends AbstractMessagingE2E {

    protected static final Logger PERF_LOG = LoggerFactory.getLogger("e2e.performance");

    protected ExecutorService executor;

    @BeforeAll
    void initExecutor() {
        executor = Executors.newFixedThreadPool(8);
    }

    @AfterAll
    void shutdownExecutor() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public record PerfResult(
            int totalRequests,
            int successes,
            int failures,
            Duration totalDuration,
            Duration avgLatency,
            Duration p95Latency,
            double throughputPerSecond) {}

    protected PerfResult measureThroughput(int requests, Callable<Boolean> operation) throws Exception {
        List<Future<Duration>> futures = new ArrayList<>();
        Instant start = Instant.now();

        for (int i = 0; i < requests; i++) {
            futures.add(executor.submit(() -> {
                Instant opStart = Instant.now();
                operation.call();
                return Duration.between(opStart, Instant.now());
            }));
        }

        List<Duration> latencies = new ArrayList<>();
        int successes = 0;
        int failures = 0;
        for (Future<Duration> future : futures) {
            try {
                latencies.add(future.get());
                successes++;
            } catch (Exception e) {
                failures++;
                PERF_LOG.warn("Operation failed: {}", e.getMessage());
            }
        }

        Duration totalDuration = Duration.between(start, Instant.now());

        if (latencies.isEmpty()) {
            return new PerfResult(requests, 0, failures, totalDuration, Duration.ZERO, Duration.ZERO, 0.0);
        }

        Collections.sort(latencies);
        long totalNanos = latencies.stream().mapToLong(Duration::toNanos).sum();
        Duration avgLatency = Duration.ofNanos(totalNanos / latencies.size());
        int p95Index = (int) Math.ceil(latencies.size() * 0.95) - 1;
        Duration p95Latency = latencies.get(Math.max(0, p95Index));
        double throughput = successes / (totalDuration.toMillis() / 1000.0);

        PerfResult result =
                new PerfResult(requests, successes, failures, totalDuration, avgLatency, p95Latency, throughput);

        PERF_LOG.info(
                "Performance: total={}, success={}, failures={}, duration={}ms, "
                        + "avgLatency={}ms, p95Latency={}ms, throughput={}/s",
                result.totalRequests(),
                result.successes(),
                result.failures(),
                result.totalDuration().toMillis(),
                result.avgLatency().toMillis(),
                result.p95Latency().toMillis(),
                String.format("%.2f", result.throughputPerSecond()));

        return result;
    }

    protected PerfResult measureSequentialBatch(int batchSize, int batchCount, Callable<Boolean> operation)
            throws Exception {
        List<PerfResult> batchResults = new ArrayList<>();

        for (int batch = 0; batch < batchCount; batch++) {
            PerfResult batchResult = measureThroughput(batchSize, operation);
            batchResults.add(batchResult);
            PERF_LOG.info(
                    "Batch {}/{} completed: success={}, throughput={}/s",
                    batch + 1,
                    batchCount,
                    batchResult.successes(),
                    String.format("%.2f", batchResult.throughputPerSecond()));
        }

        int totalRequests =
                batchResults.stream().mapToInt(PerfResult::totalRequests).sum();
        int totalSuccesses =
                batchResults.stream().mapToInt(PerfResult::successes).sum();
        int totalFailures = batchResults.stream().mapToInt(PerfResult::failures).sum();
        Duration totalDuration =
                batchResults.stream().map(PerfResult::totalDuration).reduce(Duration.ZERO, Duration::plus);

        Duration avgLatency = Duration.ZERO;
        Duration p95Latency = Duration.ZERO;
        if (!batchResults.isEmpty()) {
            long avgNanos = batchResults.stream()
                            .mapToLong(r -> r.avgLatency().toNanos())
                            .sum()
                    / batchResults.size();
            avgLatency = Duration.ofNanos(avgNanos);
            p95Latency = batchResults.stream()
                    .map(PerfResult::p95Latency)
                    .max(Duration::compareTo)
                    .orElse(Duration.ZERO);
        }

        double throughput = totalDuration.toMillis() > 0 ? totalSuccesses / (totalDuration.toMillis() / 1000.0) : 0.0;

        return new PerfResult(
                totalRequests, totalSuccesses, totalFailures, totalDuration, avgLatency, p95Latency, throughput);
    }
}
