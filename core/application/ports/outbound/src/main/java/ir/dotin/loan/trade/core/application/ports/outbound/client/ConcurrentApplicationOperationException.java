package ir.dotin.loan.trade.core.application.ports.outbound.client;

/**
 * Thrown when a distributed lock cannot be acquired for a loan application, indicating another operation is already in
 * progress.
 */
public class ConcurrentApplicationOperationException extends RuntimeException {

    private final String applicationId;

    public ConcurrentApplicationOperationException(String applicationId) {
        super("Concurrent operation already in progress for application: " + applicationId);
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }
}
