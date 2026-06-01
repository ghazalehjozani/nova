package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

/**
 * A held claim on exactly one partition of the FCB reply topic for the lifetime of this instance.
 *
 * <p>The partition number is stamped into {@code KafkaHeaders.REPLY_PARTITION} on every outbound request and pinned by
 * the manual-assign reply container, so FCB delivers each reply back to this instance. The claim is released on
 * graceful shutdown (or when the backing coordinator is invalidated) so another instance can reclaim the partition
 * without two consumers ever sharing it.
 */
public interface ReplyPartitionLease extends AutoCloseable {

    /** The leased partition index, always in {@code [0, partitionCount)}. */
    int partition();

    /** Whether the claim is still believed to be held (renewal succeeding). */
    boolean isHeld();

    /** Release the claim. Idempotent; never throws. */
    void release();

    @Override
    default void close() {
        release();
    }
}
