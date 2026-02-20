package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto;

import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * JMS response message received from FCB via ActiveMQ reply queues.
 *
 * @param correlationId the correlation ID matching the original request
 * @param success whether the operation completed successfully
 * @param errorCode FCB error code if the operation failed
 * @param errorMessage human-readable error description
 * @param payload response data as a map of key-value pairs
 */
public record FcbJmsResponse(
        String correlationId,
        boolean success,
        @Nullable String errorCode,
        @Nullable String errorMessage,
        @Nullable Map<String, Object> payload) {

    public boolean isError() {
        return !success;
    }
}
