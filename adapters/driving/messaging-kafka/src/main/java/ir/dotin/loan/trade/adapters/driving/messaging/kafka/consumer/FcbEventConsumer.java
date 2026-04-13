package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ir.dotin.platform.inbox.core.InboundEventIngestor;
import ir.dotin.platform.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.messaging.kafka.converter.KafkaInboundMessageConverter;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;

import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class FcbEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(FcbEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final KafkaInboundMessageConverter converter;
    private final InboundEventIngestor ingestor;

    public FcbEventConsumer(
            ObjectMapper objectMapper, KafkaInboundMessageConverter converter, InboundEventIngestor ingestor) {
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.ingestor = ingestor;
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
                            payloadType = Byte.class,
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
        String operationType;
        String eventUid;
        JsonNode rootNode = objectMapper.readTree(inboundMessage.payload());
        operationType = resolveOperationType(rootNode, consumerRecord);
        eventUid = resolveEventUid(rootNode, consumerRecord);

        FcbEventOperationType opType;
        try {
            opType = FcbEventOperationType.ofCode(operationType);
        } catch (IllegalArgumentException e) {
            LOG.warn("Unknown operationType [{}], eventUid={}, skipping.", operationType, eventUid);
            return;
        }
        InboundMessage message = inboundMessage.withSource(opType.getCode());
        ingestor.ingest(message);
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
            return opNode.asString();
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
            return node.asString();
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
}
