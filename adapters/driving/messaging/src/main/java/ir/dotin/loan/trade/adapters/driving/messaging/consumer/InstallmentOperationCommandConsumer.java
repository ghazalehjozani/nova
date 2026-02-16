package ir.dotin.loan.trade.adapters.driving.messaging.consumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ir.dotin.platform.adapter.messaging.command.model.RawCommandMessage;
import ir.dotin.platform.adapter.messaging.command.processor.CommandProcessor;
import ir.dotin.platform.adapter.messaging.command.serializer.CommandSerializer;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentOperationResponse;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentPaymentMessage;
import ir.dotin.loan.trade.adapters.driving.messaging.mapper.InstallmentCollectionMessageMapper;
import ir.dotin.loan.trade.adapters.driving.messaging.publisher.InstallmentOperationResponsePublisher;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand;

import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import lombok.RequiredArgsConstructor;

/**
 * Kafka consumer for the shared installment operations topic.
 *
 * <p><b>Request/Reply pattern:</b>
 *
 * <ol>
 *   <li>Old system (Java 8) publishes request to request topic via corridor
 *   <li>This consumer maps the message to a domain command (anti-corruption layer)
 *   <li>Delegates to {@link CommandProcessor} for standard pipeline processing
 *   <li>Response (success/failure) is sent to the {@code responseTopic} specified in the inbound message
 * </ol>
 *
 * <p>Since the producer (old system, Java 8) cannot use {@code @JsonTypeInfo}, this consumer reads the
 * {@code operationType} field from the message (or Kafka header) and performs manual dispatch to the correct handler.
 */
@Component
@RequiredArgsConstructor
public class InstallmentOperationCommandConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(InstallmentOperationCommandConsumer.class);

    private static final String OPERATION_TYPE_INSTALLMENT_COLLECTION = "INSTALLMENT_COLLECTION";

    private final ObjectMapper objectMapper;
    private final InstallmentCollectionMessageMapper messageMapper;
    private final CommandProcessor processor;
    private final CommandSerializer commandSerializer;
    private final InstallmentOperationResponsePublisher responsePublisher;

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
                                                        description =
                                                                "Discriminator: INSTALLMENT_COLLECTION, INSTALLMENT_PREPAYMENT, etc.",
                                                        value = "Operation type code"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "Idempotency-Key",
                                                        description = "Unique identifier for idempotency",
                                                        value = "UUID string")
                                            })))
    public void consume(ConsumerRecord<String, byte[]> consumerRecord) {
        String operationType = resolveOperationType(consumerRecord);
        String eventUid = extractHeader(consumerRecord, "eventUid");

        LOG.info(
                "Received installment operation [operationType={}, eventUid={}, key={}]",
                operationType,
                eventUid,
                consumerRecord.key());

        switch (operationType) {
            case OPERATION_TYPE_INSTALLMENT_COLLECTION -> handleInstallmentCollection(consumerRecord, eventUid);

            // future operations:
            // case "INSTALLMENT_PREPAYMENT" -> handleInstallmentPrepayment(consumerRecord, eventUid);
            // case "PENALTY_SETTLEMENT"     -> handlePenaltySettlement(consumerRecord, eventUid);
            // case "PENALTY_WAIVER"         -> handlePenaltyWaiver(consumerRecord, eventUid);

            default -> {
                LOG.warn("Unknown operationType [{}], eventUid={}, skipping.", operationType, eventUid);
                sendErrorResponse(
                        consumerRecord, eventUid, operationType, null, "Unknown operationType: " + operationType);
            }
        }
    }

    private void handleInstallmentCollection(ConsumerRecord<String, byte[]> record, String eventUid) {
        InstallmentPaymentMessage message;
        try {
            message = objectMapper.readValue(record.value(), InstallmentPaymentMessage.class);
        } catch (IOException e) {
            LOG.error("Malformed INSTALLMENT_COLLECTION message [eventUid={}]", eventUid, e);
            sendErrorResponse(
                    record,
                    eventUid,
                    OPERATION_TYPE_INSTALLMENT_COLLECTION,
                    null,
                    "Malformed message: " + e.getMessage());
            return;
        }

        LOG.info(
                "Processing INSTALLMENT_COLLECTION [eventUid={}, fileNumber={}, payments={}]",
                eventUid,
                message.fileNumber(),
                message.payments() != null ? message.payments().size() : 0);

        try {
            // Map DTO to domain command (anti-corruption layer)
            CollectInstallmentCommand command = messageMapper.toCommand(message);

            // Serialize command with type info for CommandProcessor
            byte[] commandBytes = commandSerializer.serialize(command).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            RawCommandMessage original = RawCommandMessage.from(record);
            RawCommandMessage rawMessage = new RawCommandMessage(
                    original.topic(), original.partition(), original.offset(),
                    original.key(), commandBytes, original.headers(),
                    original.timestamp(), original.responseTopic());
            processor.process(rawMessage);

            // Request/Reply: send success response
            sendSuccessResponse(message);

        } catch (Exception e) {
            LOG.error(
                    "Failed to process INSTALLMENT_COLLECTION [eventUid={}, fileNumber={}]",
                    eventUid,
                    message.fileNumber(),
                    e);
            sendErrorResponse(
                    record, eventUid, OPERATION_TYPE_INSTALLMENT_COLLECTION, message.fileNumber(), e.getMessage());
            throw new RuntimeException("INSTALLMENT_COLLECTION processing failed: " + eventUid, e);
        }
    }

    // -----------------------------------------------------------------------
    // Request/Reply response helpers
    // -----------------------------------------------------------------------

    private void sendSuccessResponse(InstallmentPaymentMessage message) {
        String responseTopic = message.responseTopic();
        if (responseTopic == null || responseTopic.isBlank()) {
            LOG.debug("No responseTopic in message [eventUid={}], skipping reply.", message.eventUid());
            return;
        }

        InstallmentOperationResponse response =
                InstallmentOperationResponse.success(message.eventUid(), message.operationType(), message.fileNumber());

        responsePublisher.sendResponse(responseTopic, response);
    }

    private void sendErrorResponse(
            ConsumerRecord<String, byte[]> record,
            String eventUid,
            String operationType,
            String fileNumber,
            String errorMessage) {
        String responseTopic = resolveResponseTopic(record);
        if (responseTopic == null || responseTopic.isBlank()) {
            LOG.debug("No responseTopic available [eventUid={}], skipping error reply.", eventUid);
            return;
        }

        InstallmentOperationResponse response = InstallmentOperationResponse.failed(
                eventUid != null ? eventUid : "UNKNOWN",
                operationType,
                fileNumber != null ? fileNumber : "UNKNOWN",
                errorMessage);

        try {
            responsePublisher.sendResponse(responseTopic, response);
        } catch (Exception e) {
            LOG.error("Failed to send error response [eventUid={}]", eventUid, e);
        }
    }

    // -----------------------------------------------------------------------
    // Header/field resolution helpers
    // -----------------------------------------------------------------------

    /** Resolves operation type: header first (cheap), then JSON body fallback. */
    private String resolveOperationType(ConsumerRecord<String, byte[]> record) {
        String fromHeader = extractHeader(record, "operationType");
        if (fromHeader != null && !fromHeader.isEmpty()) {
            return fromHeader;
        }
        try {
            JsonNode root = objectMapper.readTree(record.value());
            JsonNode opNode = root.get("operationType");
            if (opNode != null && !opNode.isNull()) {
                return opNode.asText();
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse operationType from message body", e);
        }
        return "UNKNOWN";
    }

    /**
     * Resolves the response topic from the JSON body. Used when the full message hasn't been deserialized yet (error
     * paths).
     */
    private String resolveResponseTopic(ConsumerRecord<String, byte[]> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());
            JsonNode node = root.get("responseTopic");
            if (node != null && !node.isNull()) {
                return node.asText();
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse responseTopic from message body", e);
        }
        return null;
    }

    private String extractHeader(ConsumerRecord<String, byte[]> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (header != null && header.value() != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return null;
    }
}
