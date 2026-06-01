package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import org.jspecify.annotations.Nullable;

/**
 * Fallback assigner used when no coordinated (Consul) assigner is present — i.e. test slices and Consul-less contexts.
 * Derives the partition from the instance-id via {@link FcbReplyPartitionResolver} (the legacy pod-ordinal/hash
 * scheme), and hands back a no-op lease. Carries the same collision caveat as the resolver and is NOT used in real
 * deployments, where {@code ConsulLeaseReplyPartitionAssigner} is primary.
 */
public final class StaticFcbReplyPartitionAssigner implements FcbReplyPartitionAssigner {

    // nullable by design: FcbReplyPartitionResolver falls back to pod-name/host/random when instance-id is absent
    private final @Nullable String instanceId;

    public StaticFcbReplyPartitionAssigner(@Nullable String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public ReplyPartitionLease acquire(String replyTopic, int partitionCount) {
        return new StaticLease(FcbReplyPartitionResolver.resolve(instanceId, partitionCount));
    }

    private record StaticLease(int partition) implements ReplyPartitionLease {

        @Override
        public boolean isHeld() {
            return true;
        }

        @Override
        public void release() {
            // no coordinator to release
        }
    }
}
