package ir.dotin.loan.trade.adapters.driving.messaging.consumer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ir.dotin.platform.adapter.messaging.command.model.CommandHeaders;
import ir.dotin.loan.trade.adapters.driving.messaging.consumer.handler.InstallmentOperationHandler;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentOperationResponse;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentOperationType;
import ir.dotin.loan.trade.adapters.driving.messaging.publisher.InstallmentOperationResponsePublisher;

import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;

@Component
public class InstallmentOperationCommandConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(InstallmentOperationCommandConsumer.class);

    private final ObjectMapper objectMapper;
    private final InstallmentOperationResponsePublisher responsePublisher;
    private final Map<InstallmentOperationType, InstallmentOperationHandler> handlers;

    public InstallmentOperationCommandConsumer(
            ObjectMapper objectMapper,
            InstallmentOperationResponsePublisher responsePublisher,
            List<InstallmentOperationHandler> handlerList) {
        this.objectMapper = objectMapper;
        this.responsePublisher = responsePublisher;
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(InstallmentOperationHandler::getSupportedOperationType, Function.identity()));
    }

    @KafkaListener(
            topics = "corridor.core.loan.nova.installment-operation.request.queue.v1",
            groupId = "core.loan.installment-operation.*",
            containerFactory = "byteArrayKafkaListenerContainerFactory")
    @AsyncListener(
            operation =
                    @AsyncOperation(
                            channelName = "corridor.core.loan.nova.installment-operation.request.queue.v1",
                            description = "Process nova installment operation commands (request/reply)"))
    public void consume(ConsumerRecord<String, byte[]> consumerRecord) {
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(consumerRecord.value());
        } catch (IOException e) {
            LOG.error("Failed to parse JSON body from key={}", consumerRecord.key(), e);
            return;
        }

        String operationType = getText(rootNode, "operationType", "UNKNOWN");
        String eventUid = getText(rootNode, "eventUid", null);
        String authorization = getText(rootNode, "authorization", null);
        String idempotencyKey = getText(rootNode, "idempotencyKey", null);
        String responseTopic = getText(rootNode, "responseTopic", null);

        CommandHeaders headers = CommandHeaders.builder()
                .authorizationToken(authorization)
                .idempotencyKey(idempotencyKey)
                .requestDateTime(getText(rootNode, "dateTime", null))
                .build();

        LOG.info(
                "Received installment operation [operationType={}, eventUid={}, key={}]",
                operationType,
                eventUid,
                consumerRecord.key());

        InstallmentOperationHandler handler = handlers.get(InstallmentOperationType.ofCode(operationType));

        if (handler != null) {
            handler.handle(rootNode, headers, consumerRecord, eventUid, responseTopic);
        } else {
            LOG.warn("Unknown operationType [{}], eventUid={}, skipping.", operationType, eventUid);
            sendGenericErrorResponse(eventUid, responseTopic, operationType, "Unknown operationType: " + operationType);
        }
    }

    private void sendGenericErrorResponse(
            String eventUid, String responseTopic, String operationType, String errorMessage) {

        if (responseTopic == null || responseTopic.isBlank()) {
            return;
        }

        InstallmentOperationResponse response = InstallmentOperationResponse.failed(
                eventUid != null ? eventUid : "UNKNOWN", operationType, "UNKNOWN", errorMessage);

        try {
            responsePublisher.sendResponse(responseTopic, response);
        } catch (Exception e) {
            LOG.error("Failed to send generic error response [eventUid={}]", eventUid, e);
        }
    }

    private String getText(JsonNode node, String fieldName, String defaultValue) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return defaultValue;
    }
}
