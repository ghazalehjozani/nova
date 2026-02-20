package ir.dotin.loan.trade.core.application.ports.outbound.client;

/**
 * Distributed lock port to prevent concurrent FCB operations on the same loan application. Implementations should use
 * fail-fast semantics (throw immediately if lock is held).
 */
public interface LegacyOperationGuardPort {

    /**
     * Acquires an exclusive lock for the given application ID.
     *
     * @param applicationId the loan application identifier
     * @throws ConcurrentApplicationOperationException if the lock is already held
     */
    void acquire(String applicationId);

    /**
     * Releases the lock for the given application ID.
     *
     * @param applicationId the loan application identifier
     */
    void release(String applicationId);
}
