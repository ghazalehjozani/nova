package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

/**
 * Notified when a previously-held {@link ReplyPartitionLease} can no longer be guaranteed (session renewal failed or
 * the lock was invalidated). The instance must then stop consuming its reply partition and remove itself from rotation,
 * rather than risk another instance reclaiming the same partition while this one still consumes it.
 */
@FunctionalInterface
public interface ReplyPartitionLeaseLostListener {

    void onReplyPartitionLeaseLost(String reason);
}
