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
import ir.dotin.platform.adapter.messaging.header.NovaHeader;
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
                            description = "Process nova installment operation commands (request/reply)",
                            headers =
                                    @AsyncOperation.Headers(
                                            schemaName = "InstallmentOperationHeaders",
                                            values = {
                                                @AsyncOperation.Headers.Header(
                                                        name = "Idempotency-Key",
                                                        description = "Unique identifier for idempotency (UUID v4)",
                                                        value = "UUID string"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "X-Request-DateTime",
                                                        description = "Request timestamp (ISO 8601 UTC)",
                                                        value = "2025-08-22T14:30:00.123Z"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "Accept-Language",
                                                        description = "Preferred language for error messages",
                                                        value = "fa | en-US"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "Authorization",
                                                        description = "Bearer token for authentication",
                                                        value = "Bearer token"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "traceparent",
                                                        description = "W3C trace context (distributed tracing)",
                                                        value =
                                                                "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "tracestate",
                                                        description = "W3C trace state (vendor-specific)",
                                                        value = "congo=t61rcWkgMzE"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "X-Operation-Type",
                                                        description = "Discriminator: INSTALLMENT_COLLECTION, etc.",
                                                        value = "Operation type code")
                                            })))
    public void consume(ConsumerRecord<String, byte[]> consumerRecord) {
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(consumerRecord.value());
        } catch (IOException e) {
            LOG.error("Failed to parse JSON body from key={}", consumerRecord.key(), e);
            return;
        }

        String operationType = getHeader(consumerRecord, NovaHeader.OPERATION_TYPE.getValue());
        if (operationType == null || operationType.isBlank()) {
            operationType = "UNKNOWN";
        }

        String eventUid = getHeader(consumerRecord, "eventUid");
        if (eventUid == null) {
            eventUid = getHeader(consumerRecord, NovaHeader.IDEMPOTENCY_KEY.getValue());
        }

        String responseTopic = getText(rootNode, "responseTopic", null);

        CommandHeaders headers = CommandHeaders.builder()
                .authorizationToken(getHeader(consumerRecord, NovaHeader.AUTHORIZATION.getValue()))
                .idempotencyKey(getHeader(consumerRecord, NovaHeader.IDEMPOTENCY_KEY.getValue()))
                .requestDateTime(getHeader(consumerRecord, NovaHeader.REQUEST_DATETIME.getValue()))
                .acceptLanguage(getHeader(consumerRecord, NovaHeader.ACCEPT_LANGUAGE.getValue()))
                .traceparent(getHeader(consumerRecord, NovaHeader.TRACEPARENT.getValue()))
                .tracestate(getHeader(consumerRecord, NovaHeader.TRACESTATE.getValue()))
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

    private String getHeader(ConsumerRecord<String, byte[]> record, String headerName) {
        var header = record.headers().lastHeader(headerName);
        if (header == null || header.value() == null) {
            return null;
        }
        return new String(header.value(), java.nio.charset.StandardCharsets.UTF_8);
    }
}
