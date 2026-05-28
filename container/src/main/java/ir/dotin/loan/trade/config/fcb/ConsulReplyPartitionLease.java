package ir.dotin.loan.trade.config.fcb;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.ReplyPartitionLease;

/**
 * A Consul session-backed claim on one partition of the FCB reply topic. The claim is a KV lock acquired with the
 * session; a background daemon renews the session at a fixed interval. If renewal fails (Consul unreachable or the
 * session was invalidated) the lock is considered lost and {@code onLeaseLost} fires so the instance can drain and
 * leave rotation. Crash without graceful release is covered by the session TTL — Consul auto-releases the lock when the
 * TTL expires.
 */
final class ConsulReplyPartitionLease implements ReplyPartitionLease {

    private static final Logger LOG = LoggerFactory.getLogger(ConsulReplyPartitionLease.class);

    private final ConsulClient consul;
    private final String aclToken;
    private final String sessionId;
    private final int partition;
    private final String lockKey;
    private final Consumer<String> onLeaseLost;
    private final ScheduledExecutorService renewer;
    private final AtomicBoolean held = new AtomicBoolean(true);

    ConsulReplyPartitionLease(
            ConsulClient consul,
            String aclToken,
            String sessionId,
            int partition,
            String lockKey,
            Duration renewInterval,
            Consumer<String> onLeaseLost) {
        this.consul = consul;
        this.aclToken = aclToken;
        this.sessionId = sessionId;
        this.partition = partition;
        this.lockKey = lockKey;
        this.onLeaseLost = onLeaseLost;
        this.renewer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "fcb-reply-lease-renew-p" + partition);
            t.setDaemon(true);
            return t;
        });
        long periodSeconds = Math.max(1L, renewInterval.toSeconds());
        renewer.scheduleWithFixedDelay(this::renew, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    @Override
    public int partition() {
        return partition;
    }

    @Override
    public boolean isHeld() {
        return held.get();
    }

    private void renew() {
        if (!held.get()) {
            return;
        }
        try {
            Response<Session> resp = aclToken.isBlank()
                    ? consul.renewSession(sessionId, QueryParams.DEFAULT)
                    : consul.renewSession(sessionId, QueryParams.DEFAULT, aclToken);
            if (resp == null || resp.getValue() == null) {
                loseLease("session renew returned no session (expired)");
            }
        } catch (RuntimeException e) {
            loseLease("session renew failed: " + e.getMessage());
        }
    }

    private void loseLease(String reason) {
        if (held.compareAndSet(true, false)) {
            LOG.error("FCB-REPLY-LEASE: partition={} session={} lost: {}", partition, sessionId, reason);
            // graceful shutdown (no interrupt) — we are on the renew thread; the drain runs synchronously here, then
            // the
            // executor stops scheduling because shutdown() was called and no further task is queued.
            renewer.shutdown();
            try {
                onLeaseLost.accept(reason);
            } catch (RuntimeException e) {
                LOG.warn("FCB-REPLY-LEASE: lease-lost handler failed", e);
            }
        }
    }

    @Override
    public void release() {
        held.set(false);
        renewer.shutdown();
        try {
            PutParams params = new PutParams();
            params.setReleaseSession(sessionId);
            if (aclToken.isBlank()) {
                consul.setKVValue(lockKey, "", params);
            } else {
                consul.setKVValue(lockKey, "", aclToken, params);
            }
        } catch (RuntimeException e) {
            LOG.warn("FCB-REPLY-LEASE: KV release failed for key={}", lockKey, e);
        }
        try {
            if (aclToken.isBlank()) {
                consul.sessionDestroy(sessionId, QueryParams.DEFAULT);
            } else {
                consul.sessionDestroy(sessionId, QueryParams.DEFAULT, aclToken);
            }
        } catch (RuntimeException e) {
            LOG.warn("FCB-REPLY-LEASE: session destroy failed for session={}", sessionId, e);
        }
        LOG.info("FCB-REPLY-LEASE: released partition={} session={}", partition, sessionId);
    }
}
