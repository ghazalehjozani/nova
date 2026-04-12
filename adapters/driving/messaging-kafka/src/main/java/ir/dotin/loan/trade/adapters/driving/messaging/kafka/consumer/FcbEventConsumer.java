package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

import ir.dotin.platform.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.messaging.api.inbound.InboundMessageHeaders;
import ir.dotin.platform.messaging.kafka.converter.KafkaInboundMessageConverter;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;
import ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler.FcbEventOperationHandler;

import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;

@Component
public class FcbEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(FcbEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final KafkaInboundMessageConverter converter;
    private final Map<FcbEventOperationType, FcbEventOperationHandler> handlers;

    public FcbEventConsumer(
            ObjectMapper objectMapper,
            KafkaInboundMessageConverter converter,
            List<FcbEventOperationHandler> handlerList) {
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(FcbEventOperationHandler::getSupportedOperationType, Function.identity()));
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
                                                        name = "eventUid",
                                                        description = "Unique event identifier (GUID)",
                                                        value = "UUID string"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "operationType",
                                                        description = "Discriminator: INSTALLMENT_COLLECTION, etc.",
                                                        value = "Operation type code"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "Idempotency-Key",
                                                        description = "Unique identifier for idempotency",
                                                        value = "UUID string")
                                            })))
    public void consume(ConsumerRecord<String, byte[]> consumerRecord) {
        InboundMessage inboundMessage = converter.convert(consumerRecord);
        validateRequiredHeaders(inboundMessage.headers());
        InboundMessageHeaders headers = inboundMessage.headers();

        String operationType;
        String eventUid;
        try {
            JsonNode rootNode = objectMapper.readTree(inboundMessage.payload());
            operationType = resolveOperationType(rootNode, consumerRecord);
            eventUid = resolveEventUid(rootNode, consumerRecord);

            LOG.info(
                    "Received installment operation [operationType={}, eventUid={}, key={}]",
                    operationType,
                    eventUid,
                    consumerRecord.key());

            FcbEventOperationType opType;
            try {
                opType = FcbEventOperationType.ofCode(operationType);
            } catch (IllegalArgumentException e) {
                LOG.warn("Unknown operationType [{}], eventUid={}, skipping.", operationType, eventUid);
                return;
            }

            FcbEventOperationHandler handler = handlers.get(opType);
            if (handler == null) {
                LOG.warn("No handler registered for operationType [{}], eventUid={}", operationType, eventUid);
                return;
            }

            handler.handle(rootNode, headers, inboundMessage, eventUid);

        } catch (IOException e) {
            LOG.error(
                    "Failed to parse installment operation message [key={}]: {}",
                    consumerRecord.key(),
                    e.getMessage(),
                    e);
            throw new RuntimeException("Installment operation message parsing failed", e);
        }
    }

    private String resolveOperationType(JsonNode rootNode, ConsumerRecord<String, byte[]> record) {
        // 1. Try header first (cheap)
        String fromHeader = extractHeader(record, "operationType");
        if (fromHeader != null && !fromHeader.isEmpty()) {
            return fromHeader;
        }
        // 2. Fallback to body
        JsonNode opNode = rootNode.get("operationType");
        if (opNode != null && !opNode.isNull()) {
            return opNode.asText();
        }
        return "UNKNOWN";
    }

    private String resolveEventUid(JsonNode rootNode, ConsumerRecord<String, byte[]> record) {
        String fromHeader = extractHeader(record, "eventUid");
        if (fromHeader != null && !fromHeader.isEmpty()) {
            return fromHeader;
        }
        JsonNode node = rootNode.get("eventUid");
        if (node != null && !node.isNull()) {
            return node.asText();
        }
        return null;
    }

    private String extractHeader(ConsumerRecord<String, byte[]> record, String headerName) {
        var header = record.headers().lastHeader(headerName);
        if (header != null && header.value() != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return null;
    }

    private void validateRequiredHeaders(InboundMessageHeaders headers) {
        if (headers.idempotencyKey() == null) {
            throw new IllegalArgumentException("Missing required header: Idempotency-Key");
        }
        if (headers.requestDateTime() == null) {
            throw new IllegalArgumentException("Missing required header: X-Request-DateTime");
        }
        if (headers.acceptLanguage() == null) {
            throw new IllegalArgumentException("Missing required header: Accept-Language");
        }
        if (headers.authorizationToken() == null) {
            throw new IllegalArgumentException("Missing required header: Authorization");
        }
        if (headers.traceparent() == null) {
            throw new IllegalArgumentException("Missing required header: traceparent");
        }
    }
}
