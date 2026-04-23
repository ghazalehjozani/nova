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
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.HeartbeatRequest;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * Sends synthetic heartbeat messages to the FCB consumer on a fixed cadence.
 *
 * <p>Runs on a single dedicated virtual thread with state-aware sleep (faster when DOWN so recovery is detected
 * quickly). The probe goes through the same {@link ReplyingKafkaTemplate} as user traffic, so a passing probe proves
 * the whole request-reply path (broker, ACLs, topic partitions, consumer group, dispatcher, handler, reply topic) is
 * alive.
 */
@Slf4j
@Component
public class FcbHealthProbe {

    private final ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FcbKafkaProperties kafkaProperties;
    private final FcbHealthProperties healthProperties;
    private final FcbHealthState state;
    private final FcbHealthMetrics metrics;
    private final Clock clock;

    private volatile Thread probeThread;
    private volatile boolean stopped;

    public FcbHealthProbe(
            ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate,
            ObjectMapper objectMapper,
            FcbKafkaProperties kafkaProperties,
            FcbHealthProperties healthProperties,
            FcbHealthState state,
            FcbHealthMetrics metrics,
            Clock clock) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.objectMapper = objectMapper;
        this.kafkaProperties = kafkaProperties;
        this.healthProperties = healthProperties;
        this.state = state;
        this.metrics = metrics;
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
        HeartbeatRequest req = new HeartbeatRequest(eventUid, Instant.now(clock).toEpochMilli());
        req.setProducerCode(healthProperties.producerCode());
        req.setEventUid(eventUid);
        req.setDateTime(Date.from(Instant.now(clock)));
        req.setVersion(1);

        long start = System.nanoTime();
        try {
            byte[] payload = objectMapper.writeValueAsBytes(req);

            ProducerRecord<String, byte[]> record =
                    new ProducerRecord<>(kafkaProperties.requestTopic(), eventUid, payload);
            record.headers()
                    .add(new RecordHeader(
                            "X-Operation-Type",
                            healthProperties.heartbeatOperationName().getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader("eventUid", eventUid.getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader("X-Health-Probe", "true".getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(
                            KafkaHeaders.REPLY_TOPIC,
                            kafkaProperties.replyTopic().getBytes(StandardCharsets.UTF_8)));

            RequestReplyFuture<String, byte[], byte[]> future =
                    replyingKafkaTemplate.sendAndReceive(record, healthProperties.probeTimeout());
            ConsumerRecord<String, byte[]> reply =
                    future.get(healthProperties.probeTimeout().toMillis(), TimeUnit.MILLISECONDS);

            long elapsed = System.nanoTime() - start;
            validateReply(reply);
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

    private void validateReply(ConsumerRecord<String, byte[]> reply) throws Exception {
        if (reply == null || reply.value() == null || reply.value().length == 0) {
            throw new IllegalStateException("empty heartbeat reply");
        }
        FcbKafkaBaseResponse resp = objectMapper.readValue(reply.value(), FcbKafkaBaseResponse.class);
        if (resp.isError()) {
            throw new IllegalStateException(
                    "heartbeat reply reported error: " + resp.getErrorCode() + " " + resp.getErrorMessage());
        }
    }

    private void sleepQuietly(Duration d) {
        if (d.isZero() || d.isNegative()) {
            return;
        }
        long nanos = d.toNanos();
        LockSupport.parkNanos(nanos);
        if (Thread.currentThread().isInterrupted()) {
            // Propagate up by letting the loop condition handle it.
        }
    }
}
