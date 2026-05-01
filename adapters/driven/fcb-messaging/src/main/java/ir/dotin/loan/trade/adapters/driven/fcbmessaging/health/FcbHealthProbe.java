package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.HeartbeatRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response.HeartbeatKafkaResponse;
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
    private static final String UNKNOWN_NODE = "unknown";

    private final ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FcbKafkaProperties kafkaProperties;
    private final FcbHealthProperties healthProperties;
    private final FcbHealthState state;
    private final FcbRemoteHealthState remoteState;
    private final FcbHealthMetrics metrics;
    private final Tracer tracer;
    private final Clock clock;

    private volatile Thread probeThread;
    private volatile boolean stopped;

    public FcbHealthProbe(
            ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate,
            ObjectMapper objectMapper,
            FcbKafkaProperties kafkaProperties,
            FcbHealthProperties healthProperties,
            FcbHealthState state,
            FcbRemoteHealthState remoteState,
            FcbHealthMetrics metrics,
            Tracer tracer,
            Clock clock) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.objectMapper = objectMapper;
        this.kafkaProperties = kafkaProperties;
        this.healthProperties = healthProperties;
        this.state = state;
        this.remoteState = remoteState;
        this.metrics = metrics;
        this.tracer = tracer;
        this.clock = clock;
    }

    @PostConstruct
    public void start() {
        if (!healthProperties.enabled()) {
            log.info("FCB Kafka health probe disabled by configuration.");
            return;
        }
        this.probeThread = Thread.ofVirtual().name("fcb-kafka-health-probe").start(this::loop);
        log.info(
                "FCB Kafka health probe started: interval={} recovery={} timeout={}",
                healthProperties.probeInterval(),
                healthProperties.recoveryProbeInterval(),
                healthProperties.probeTimeout());
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
    }

    private void loop() {
        sleepQuietly(healthProperties.initialDelay());
        while (!stopped && !Thread.currentThread().isInterrupted()) {
            try {
                probeOnce();
            } catch (Throwable t) {
                log.error("Uncaught error in health probe loop", t);
                state.recordProbeFailure(t.getClass().getSimpleName() + ": " + t.getMessage());
                metrics.recordProbeFailure();
            }
            Duration sleep = state.status() == FcbHealthStatus.DOWN
                    ? healthProperties.recoveryProbeInterval()
                    : healthProperties.probeInterval();
            sleepQuietly(sleep);
        }
    }

    private void probeOnce() {
        String eventUid = UUID.randomUUID().toString();
        HeartbeatRequest request =
                new HeartbeatRequest(eventUid, Instant.now(clock).toEpochMilli());
        request.setProducerCode(healthProperties.producerCode());
        request.setEventUid(eventUid);
        request.setDateTime(Date.from(Instant.now(clock)));
        request.setVersion(1);

        long timestampMs = System.currentTimeMillis();
        long deadlineMs = timestampMs + healthProperties.probeTimeout().toMillis();
        long start = System.nanoTime();
        try {
            byte[] payload = objectMapper.writeValueAsBytes(request);

            ProducerRecord<String, byte[]> record =
                    new ProducerRecord<>(kafkaProperties.requestTopic(), eventUid, payload);
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
                    .add(new RecordHeader(
                            HEADER_IDEMPOTENCY_KEY, request.getEventUid().getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(
                            HEADER_REQUEST_DATETIME,
                            request.getDateTime().toInstant().toString().getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(
                            KafkaHeaders.REPLY_TOPIC,
                            kafkaProperties.replyTopic().getBytes(StandardCharsets.UTF_8)));
            addTracingHeaders(record);

            RequestReplyFuture<String, byte[], byte[]> future =
                    replyingKafkaTemplate.sendAndReceive(record, healthProperties.probeTimeout());
            ConsumerRecord<String, byte[]> reply;
            try {
                reply = future.get(healthProperties.probeTimeout().toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                future.cancel(true);
                throw ex;
            }

            long elapsed = System.nanoTime() - start;
            validateAndCaptureRemote(reply);
            Duration latency = Duration.ofNanos(elapsed);
            state.recordProbeSuccess(latency);
            metrics.recordProbeSuccess(elapsed);
            if (log.isTraceEnabled()) {
                log.trace("FCB heartbeat probe ok: latencyMs={}", latency.toMillis());
            }
        } catch (Throwable t) {
            String error = t.getClass().getSimpleName() + ": " + (t.getMessage() == null ? "" : t.getMessage());
            state.recordProbeFailure(error);
            metrics.recordProbeFailure();
            log.warn("FCB heartbeat probe failed: {}", error);
        }
    }

    private void validateAndCaptureRemote(ConsumerRecord<String, byte[]> reply) {
        if (reply == null || reply.value() == null || reply.value().length == 0) {
            throw new IllegalStateException("empty heartbeat reply");
        }
        HeartbeatKafkaResponse resp = objectMapper.readValue(reply.value(), HeartbeatKafkaResponse.class);
        if (resp.isError()) {
            throw new IllegalStateException(
                    "heartbeat reply reported error: " + resp.getErrorCode() + " " + resp.getErrorMessage());
        }

        String host = resp.getConsumerNode();
        if (host == null || host.trim().isEmpty()) {
            host = UNKNOWN_NODE;
        }

        remoteState.updateForHost(
                host,
                new FcbRemoteHealthSnapshot(
                        host,
                        resp.getHealthStatus(),
                        resp.getTotalComponents(),
                        resp.getUpComponents(),
                        resp.getDegradedComponents(),
                        resp.getDownComponents(),
                        resp.getLastHealthChangeAtEpochMs(),
                        Instant.now(clock)));
    }

    private void sleepQuietly(Duration d) {
        if (d.isZero() || d.isNegative()) {
            return;
        }
        long nanos = d.toNanos();
        LockSupport.parkNanos(nanos);
    }

    private void addTracingHeaders(ProducerRecord<String, byte[]> record) {
        if (tracer == null || tracer.currentSpan() == null) {
            return;
        }
        TraceContext ctx = tracer.currentSpan().context();
        String sampledFlag = Boolean.TRUE.equals(ctx.sampled()) ? "01" : "00";
        String traceparent = "00-" + ctx.traceId() + "-" + ctx.spanId() + "-" + sampledFlag;
        record.headers().add(new RecordHeader(HEADER_TRACEPARENT, traceparent.getBytes(StandardCharsets.UTF_8)));
    }
}
