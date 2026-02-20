package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * JMS request message sent to FCB via ActiveMQ queues.
 *
 * @param correlationId unique identifier for request-reply correlation
 * @param operationName the FCB usecase name (e.g. "load-economicalSection-by-code")
 * @param parameters list of key-value parameters for the operation
 * @param applicationId optional loan application ID for context/locking
 */
public record FcbJmsRequest(
        String correlationId,
        String operationName,
        List<FcbJmsParameter> parameters,
        @Nullable String applicationId) {}
