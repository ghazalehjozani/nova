package ir.dotin.loan.trade.adapters.driven.fcbmessaging;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaConfig;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.ReplyPartitionLease;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.ReplyPartitionLeaseLostListener;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.health.FcbReplyLeaseState;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.service.FcbKafkaClient;

import lombok.extern.slf4j.Slf4j;

/**
 * Drains in-flight FCB request/reply calls before this instance gives up its reply partition, so a scale-down or a lost
 * lease never orphans a reply that is still being awaited.
 *
 * <p>Triggered from two paths, both running the same sequence:
 *
 * <ol>
 *   <li><b>Graceful shutdown</b> — {@link SmartLifecycle#stop()} at the highest phase, so this runs <em>before</em> the
 *       Kafka listener containers are stopped by Spring's normal lifecycle.
 *   <li><b>Lost lease</b> — {@link #onReplyPartitionLeaseLost(String)} from the Consul lease's renew loop.
 * </ol>
 *
 * <p>Sequence: mark readiness DOWN (stop new traffic to this pod) → stop accepting new sends → wait for outstanding
 * calls up to {@code lease.drain-timeout} → stop the integration reply container → release the partition lease.
 */
@Slf4j
@Component
@Profile("kafka-fcb")
public class FcbReplyDrainCoordinator implements SmartLifecycle, ReplyPartitionLeaseLostListener {

    private final FcbKafkaClient client;
    private final ConcurrentMessageListenerContainer<String, byte[]> integrationContainer;
    private final ReplyPartitionLease lease;
    private final FcbReplyLeaseState leaseState;
    private final Duration drainTimeout;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public FcbReplyDrainCoordinator(
            FcbKafkaClient client,
            @Qualifier(FcbKafkaConfig.FCB_INTEGRATION_REPLIES_CONTAINER)
                    ConcurrentMessageListenerContainer<String, byte[]> integrationContainer,
            ReplyPartitionLease lease,
            FcbReplyLeaseState leaseState,
            FcbKafkaProperties properties) {
        this.client = client;
        this.integrationContainer = integrationContainer;
        this.lease = lease;
        this.leaseState = leaseState;
        this.drainTimeout = properties.getLease().getDrainTimeout();
    }

    @Override
    public void start() {
        running.set(true);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            drainAndRelease("application shutdown");
        }
    }

    /**
     * Highest phase so Spring invokes {@link #stop()} before stopping the Kafka listener containers — the reply
     * consumer must stay up while we wait for outstanding replies.
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onReplyPartitionLeaseLost(String reason) {
        log.error("FCB-REPLY-LEASE: lease lost ({}) — draining and removing instance from rotation", reason);
        running.set(false);
        drainAndRelease("lease lost: " + reason);
    }

    private synchronized void drainAndRelease(String reason) {
        leaseState.markDegraded(reason);
        client.beginDrain();
        boolean drained = client.awaitDrain(drainTimeout);
        if (drained) {
            log.info("FCB-REPLY-DRAIN: drained cleanly (reason={})", reason);
        } else {
            log.warn(
                    "FCB-REPLY-DRAIN: {} in-flight request(s) still pending after {} — stopping anyway (reason={})",
                    client.inFlightCount(),
                    drainTimeout,
                    reason);
        }
        try {
            integrationContainer.stop();
        } catch (RuntimeException e) {
            log.warn("FCB-REPLY-DRAIN: error stopping integration reply container", e);
        }
        try {
            lease.release();
        } catch (RuntimeException e) {
            log.warn("FCB-REPLY-LEASE: error releasing partition lease", e);
        }
    }
}
