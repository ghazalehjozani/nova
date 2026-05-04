package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.HeartbeatRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.util.HostResolver;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class FcbHealthProbe {

    private static final String HEADER_OPERATION_TYPE = "X-Operation-Type";
    private static final String HEADER_EVENT_UID = "eventUid";
    private static final String HEADER_HEALTH_PROBE = "X-Health-Probe";
    private static final String HEADER_REQUEST_TIMESTAMP_EPOCH_MS = "X-Request-Timestamp-Epoch-Ms";
    private static final String HEADER_REQUEST_DEADLINE_EPOCH_MS = "X-Request-Deadline-Epoch-Ms";
    private static final String HEADER_HOST = "X-Host";
    private static final String HEADER_TRACEPARENT = "traceparent";
    private static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";
    private static final String HEADER_REQUEST_DATETIME = "X-Request-DateTime";

    private final ReplyingKafkaTemplate<String, byte[], byte[]> healthReplyingKafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FcbKafkaProperties kafkaProperties;
    private final FcbHealthProperties healthProperties;
    private final FcbPartitionHealthRegistry partitionRegistry;
    private final FcbHealthMetrics metrics;
    private final Tracer tracer;
    private final Clock clock;

    private final AtomicLong lastSuccessfulCycleMs = new AtomicLong();
    private volatile Thread probeThread;
    private volatile ExecutorService probeExecutor;
    private volatile boolean stopped;

    public FcbHealthProbe(
            @Qualifier("fcbHealthReplyingKafkaTemplate")
                    ReplyingKafkaTemplate<String, byte[], byte[]> healthReplyingKafkaTemplate,
            ObjectMapper objectMapper,
            FcbKafkaProperties kafkaProperties,
            FcbHealthProperties healthProperties,
            FcbPartitionHealthRegistry partitionRegistry,
            FcbHealthMetrics metrics,
            Tracer tracer,
            Clock clock) {
        this.healthReplyingKafkaTemplate = healthReplyingKafkaTemplate;
        this.objectMapper = objectMapper;
        this.kafkaProperties = kafkaProperties;
        this.healthProperties = healthProperties;
        this.partitionRegistry = partitionRegistry;
        this.metrics = metrics;
        this.tracer = tracer;
        this.clock = clock;
    }

    @PostConstruct
    public void start() {
        if (!healthProperties.enabled()) return;
        this.probeExecutor = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("fcb-probe-worker-", 0).factory());
        this.probeThread = Thread.ofVirtual().name("fcb-kafka-health-probe").start(this::loop);
    }

    @PreDestroy
    public void stop() {
        stopped = true;
        Thread t = this.probeThread;
        if (t != null) {
            t.interrupt();
            try {
                t.join(Duration.ofSeconds(5));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (probeExecutor != null) {
            probeExecutor.shutdownNow();
            try {
                if (!probeExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("FCB-PROBE: Executor did not terminate cleanly");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void loop() {
        sleepQuietly(healthProperties.initialDelay());
        while (!stopped && !Thread.currentThread().isInterrupted()) {
            try {
                runCycle();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Throwable t) {
                log.warn("FCB-PROBE: cycle failed", t);
                metrics.recordProbeFailure();
            }
            checkWatchdog();
            Duration sleep = partitionRegistry.getHealthyPartitions().isEmpty()
                    ? healthProperties.recoveryProbeInterval()
                    : healthProperties.probeInterval();
            sleepQuietly(sleep);
        }
    }

    private void runCycle() throws InterruptedException {
        List<PartitionInfo> partitions =
                healthReplyingKafkaTemplate.partitionsFor(kafkaProperties.healthRequestTopic());
        if (partitions == null || partitions.isEmpty()) return;

        var futures = partitions.stream()
                .map(p -> probeExecutor.submit(() -> probePartitionOnce(p.partition())))
                .toList();

        long deadlineNanos = System.nanoTime()
                + healthProperties.probeTimeout().multipliedBy(2).toNanos();

        for (var f : futures) {
            long remaining = deadlineNanos - System.nanoTime();
            if (remaining <= 0) {
                f.cancel(true);
                continue;
            }
            try {
                f.get(remaining, TimeUnit.NANOSECONDS);
            } catch (Exception e) {
                f.cancel(true);
            }
        }
        lastSuccessfulCycleMs.set(System.currentTimeMillis());
    }

    private void checkWatchdog() {
        long last = lastSuccessfulCycleMs.get();
        if (last == 0) return;
        long staleMs = healthProperties.probeInterval().toMillis() * 2L
                + healthProperties.probeTimeout().toMillis();
        if (System.currentTimeMillis() - last > staleMs) {
            log.warn("FCB-PROBE: watchdog tripped (no cycle in {}ms) - marking all unhealthy", staleMs);
            partitionRegistry.markAllUnhealthy();
        }
    }

    private void probePartitionOnce(int partition) {
        String eventUid = UUID.randomUUID().toString();
        HeartbeatRequest request = HeartbeatRequest.builder()
                .probeId(eventUid)
                .publishedAtEpochMs(Instant.now(clock).toEpochMilli())
                .build();

        request.setProducerCode(healthProperties.producerCode());
        request.setEventUid(eventUid);
        request.setDateTime(Date.from(Instant.now(clock)));
        request.setVersion(1);

        long timestampMs = clock.millis();
        long deadlineMs = timestampMs + healthProperties.probeTimeout().toMillis();
        long start = System.nanoTime();

        try {
            byte[] payload = objectMapper.writeValueAsBytes(request);
            ProducerRecord<String, byte[]> record =
                    new ProducerRecord<>(kafkaProperties.healthRequestTopic(), partition, eventUid, payload);

            record.headers()
                    .add(new RecordHeader(
                            HEADER_OPERATION_TYPE,
                            healthProperties.heartbeatOperationName().getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(HEADER_EVENT_UID, eventUid.getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(HEADER_HEALTH_PROBE, "true".getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(
                            HEADER_REQUEST_TIMESTAMP_EPOCH_MS,
                            Long.toString(timestampMs).getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(
                            HEADER_REQUEST_DEADLINE_EPOCH_MS,
                            Long.toString(deadlineMs).getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(
                            HEADER_HOST, HostResolver.resolveHostName().getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(HEADER_IDEMPOTENCY_KEY, eventUid.getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(
                            HEADER_REQUEST_DATETIME,
                            request.getDateTime().toInstant().toString().getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(
                            KafkaHeaders.REPLY_TOPIC,
                            kafkaProperties.healthReplyTopic().getBytes(StandardCharsets.UTF_8)));

            addTracingHeaders(record);

            RequestReplyFuture<String, byte[], byte[]> future =
                    healthReplyingKafkaTemplate.sendAndReceive(record, healthProperties.probeTimeout());
            ConsumerRecord<String, byte[]> reply;
            try {
                reply = future.get(healthProperties.probeTimeout().toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                future.cancel(true);
                throw ex;
            }

            if (reply == null || reply.value() == null || reply.value().length == 0) {
                throw new IllegalStateException("empty heartbeat reply");
            }

            partitionRegistry.recordSuccess(partition);
            metrics.recordProbeSuccess(System.nanoTime() - start);

        } catch (Throwable t) {
            partitionRegistry.recordFailure(partition);
            metrics.recordProbeFailure();
            log.debug("FCB-PROBE: partition={} probe failed: {}", partition, t.toString());
        }
    }

    private void sleepQuietly(Duration d) {
        if (d.isZero() || d.isNegative()) return;
        try {
            Thread.sleep(d);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void addTracingHeaders(ProducerRecord<String, byte[]> record) {
        if (tracer == null || tracer.currentSpan() == null) return;
        TraceContext ctx = tracer.currentSpan().context();
        String sampledFlag = Boolean.TRUE.equals(ctx.sampled()) ? "01" : "00";
        String traceparent = "00-" + ctx.traceId() + "-" + ctx.spanId() + "-" + sampledFlag;
        record.headers().add(new RecordHeader(HEADER_TRACEPARENT, traceparent.getBytes(StandardCharsets.UTF_8)));
    }
}
