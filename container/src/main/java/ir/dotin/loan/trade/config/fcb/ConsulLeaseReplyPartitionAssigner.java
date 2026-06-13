package ir.dotin.loan.trade.config.fcb;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbReplyPartitionAssigner;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.ReplyPartitionLease;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.ReplyPartitionLeaseLostListener;

import io.opentelemetry.api.trace.Tracer;

/**
 * Coordinates reply-partition assignment through Consul so each live instance owns a unique partition regardless of pod
 * ordinal/hostname. Creates a TTL session, then scans partition indices {@code [0, partitionCount)} and takes the first
 * free one with a session-bound KV lock. Fails fast when no partition is free (more instances than partitions). The
 * returned {@link ConsulReplyPartitionLease} renews the session and notifies the registered
 * {@link ReplyPartitionLeaseLostListener}s if the lease is lost.
 */
final class ConsulLeaseReplyPartitionAssigner implements FcbReplyPartitionAssigner {

    private static final Logger LOG = LoggerFactory.getLogger(ConsulLeaseReplyPartitionAssigner.class);
    private static final long CONSUL_MIN_TTL_SECONDS = 10L;

    private final ConsulClient consul;
    private final String aclToken;
    private final FcbKafkaProperties.Lease leaseProps;
    private final String instanceId;
    private final ObjectProvider<ReplyPartitionLeaseLostListener> listeners;
    private final Tracer tracer;

    ConsulLeaseReplyPartitionAssigner(
            ConsulClient consul,
            String aclToken,
            FcbKafkaProperties properties,
            String instanceId,
            ObjectProvider<ReplyPartitionLeaseLostListener> listeners,
            Tracer tracer) {
        this.consul = consul;
        this.aclToken = aclToken == null ? "" : aclToken;
        this.leaseProps = properties.getLease();
        this.instanceId = instanceId == null ? "" : instanceId;
        this.listeners = listeners;
        this.tracer = tracer;
    }

    @Override
    public ReplyPartitionLease acquire(String replyTopic, int partitionCount) {
        long deadlineNanos = System.nanoTime() + leaseProps.getAcquireTimeout().toNanos();
        String sessionId = createSession();
        try {
            for (int partition = 0; partition < partitionCount; partition++) {
                String lockKey = lockKey(partition);
                if (tryAcquire(lockKey, sessionId, partition)) {
                    LOG.info(
                            "FCB-REPLY-LEASE: acquired partition={} on reply topic '{}' (session={}, key={})",
                            partition,
                            replyTopic,
                            sessionId,
                            lockKey);
                    return new ConsulReplyPartitionLease(
                            consul,
                            aclToken,
                            sessionId,
                            partition,
                            lockKey,
                            leaseProps.getRenewInterval(),
                            this::notifyLeaseLost,
                            tracer);
                }
                if (System.nanoTime() > deadlineNanos) {
                    break;
                }
            }
        } catch (RuntimeException e) {
            destroyQuietly(sessionId);
            throw new IllegalStateException(
                    "FCB-REPLY-LEASE: error while acquiring a reply-partition lease on '" + replyTopic + "'", e);
        }
        destroyQuietly(sessionId);
        throw new IllegalStateException("FCB-REPLY-LEASE: no free reply partition in [0," + partitionCount
                + ") on topic '" + replyTopic + "' — all partitions are leased by other live instances. Increase the "
                + "reply-topic partition count (and FCB provisioning) or scale down. Failing fast.");
    }

    private void notifyLeaseLost(String reason) {
        listeners.orderedStream().forEach(listener -> {
            try {
                listener.onReplyPartitionLeaseLost(reason);
            } catch (RuntimeException e) {
                LOG.warn(
                        "FCB-REPLY-LEASE: lease-lost listener {} failed",
                        listener.getClass().getSimpleName(),
                        e);
            }
        });
    }

    private String createSession() {
        NewSession session = new NewSession();
        session.setName("nova-fcb-reply-partition");
        session.setTtl(toConsulTtl(leaseProps.getSessionTtl()));
        session.setLockDelay(Math.max(0L, leaseProps.getLockDelay().toSeconds()));
        session.setBehavior(Session.Behavior.RELEASE);
        Response<String> resp = aclToken.isBlank()
                ? consul.sessionCreate(session, QueryParams.DEFAULT)
                : consul.sessionCreate(session, QueryParams.DEFAULT, aclToken);
        String sessionId = resp == null ? null : resp.getValue();
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalStateException("FCB-REPLY-LEASE: Consul sessionCreate returned no session id");
        }
        return sessionId;
    }

    private boolean tryAcquire(String lockKey, String sessionId, int partition) {
        PutParams params = new PutParams();
        params.setAcquireSession(sessionId);
        String value = "{\"instance\":\"" + escape(instanceId) + "\",\"partition\":" + partition + ",\"session\":\""
                + sessionId + "\"}";
        Response<Boolean> resp = aclToken.isBlank()
                ? consul.setKVValue(lockKey, value, params)
                : consul.setKVValue(lockKey, value, aclToken, params);
        return resp != null && Boolean.TRUE.equals(resp.getValue());
    }

    private void destroyQuietly(String sessionId) {
        try {
            if (aclToken.isBlank()) {
                consul.sessionDestroy(sessionId, QueryParams.DEFAULT);
            } else {
                consul.sessionDestroy(sessionId, QueryParams.DEFAULT, aclToken);
            }
        } catch (RuntimeException e) {
            LOG.warn("FCB-REPLY-LEASE: failed to destroy session={} after a failed acquire", sessionId, e);
        }
    }

    private String lockKey(int partition) {
        String prefix = leaseProps.getKvPrefix();
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix + "/" + partition;
    }

    private static String toConsulTtl(java.time.Duration ttl) {
        return Math.max(CONSUL_MIN_TTL_SECONDS, ttl.toSeconds()) + "s";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
