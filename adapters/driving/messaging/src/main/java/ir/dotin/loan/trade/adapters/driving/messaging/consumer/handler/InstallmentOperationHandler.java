package ir.dotin.loan.trade.adapters.driving.messaging.consumer.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import ir.dotin.platform.adapter.messaging.command.model.CommandHeaders;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentOperationType;

public interface InstallmentOperationHandler {
    /** Returns the operation type string this handler supports. */
    InstallmentOperationType getSupportedOperationType();

    /** Handles the specific business logic for the operation. */
    void handle(
            JsonNode rootNode,
            CommandHeaders headers,
            ConsumerRecord<String, byte[]> record,
            String eventUid,
            String responseTopic);
}
