package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler;

import com.fasterxml.jackson.databind.JsonNode;

import ir.dotin.platform.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.messaging.api.inbound.InboundMessageHeaders;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;

public interface FcbEventOperationHandler {

    /** Returns the operation type this handler supports. */
    FcbEventOperationType getSupportedOperationType();

    /**
     * Handles the specific business logic for the operation.
     *
     * @param rootNode the parsed JSON root of the message body
     * @param headers transport-agnostic inbound headers
     * @param message the inbound message (response destination available via {@code message.responseDestination()})
     * @param eventUid the event correlation identifier
     */
    void handle(JsonNode rootNode, InboundMessageHeaders headers, InboundMessage message, String eventUid);
}
