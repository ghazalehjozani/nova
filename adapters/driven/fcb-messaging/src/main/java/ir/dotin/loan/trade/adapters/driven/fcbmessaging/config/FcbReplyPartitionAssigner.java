package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

/**
 * Assigns this instance a unique partition of the FCB reply topic.
 *
 * <p>Implementations decide <em>how</em> uniqueness is coordinated. The production implementation
 * ({@code ConsulLeaseReplyPartitionAssigner}, in the container module) takes a Consul session-backed KV lock so
 * assignment is independent of pod ordinal/hostname and is auto-released on scale-down or crash. The fallback
 * {@link StaticFcbReplyPartitionAssigner} derives a partition from the instance-id for tests / Consul-less contexts.
 */
public interface FcbReplyPartitionAssigner {

    /**
     * Claim one partition of {@code replyTopic}. Must return a held lease or throw — implementations that cannot
     * guarantee a unique partition (e.g. all partitions already leased) MUST fail fast rather than return a colliding
     * one.
     *
     * @param replyTopic the reply topic name (informational / lock-namespacing)
     * @param partitionCount the number of partitions provisioned on the reply topic; the claim must be in {@code [0,
     *     partitionCount)}
     */
    ReplyPartitionLease acquire(String replyTopic, int partitionCount);
}
