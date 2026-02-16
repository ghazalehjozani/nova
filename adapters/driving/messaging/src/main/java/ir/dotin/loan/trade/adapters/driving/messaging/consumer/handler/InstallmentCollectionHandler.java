package ir.dotin.loan.trade.adapters.driving.messaging.consumer.handler;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.adapter.messaging.command.model.CommandHeaders;
import ir.dotin.platform.adapter.messaging.command.model.CommandResponse;
import ir.dotin.platform.adapter.messaging.command.model.RawCommandMessage;
import ir.dotin.platform.adapter.messaging.command.processor.CommandProcessor;
import ir.dotin.platform.adapter.messaging.command.serializer.CommandSerializer;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentOperationResponse;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentOperationType;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentPaymentMessage;
import ir.dotin.loan.trade.adapters.driving.messaging.mapper.InstallmentCollectionMessageMapper;
import ir.dotin.loan.trade.adapters.driving.messaging.publisher.InstallmentOperationResponsePublisher;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstallmentCollectionHandler implements InstallmentOperationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(InstallmentCollectionHandler.class);
    private static final InstallmentOperationType OPERATION_TYPE = InstallmentOperationType.INSTALLMENT_COLLECTION;

    private final ObjectMapper objectMapper;
    private final InstallmentCollectionMessageMapper messageMapper;
    private final CommandProcessor processor;
    private final CommandSerializer commandSerializer;
    private final InstallmentOperationResponsePublisher responsePublisher;

    @Override
    public InstallmentOperationType getSupportedOperationType() {
        return OPERATION_TYPE;
    }

    @Override
    public void handle(
            JsonNode rootNode,
            CommandHeaders headers,
            ConsumerRecord<String, byte[]> record,
            String eventUid,
            String responseTopic) {
        InstallmentPaymentMessage message;
        try {
            message = objectMapper.treeToValue(rootNode, InstallmentPaymentMessage.class);
        } catch (Exception e) {
            LOG.error("Malformed INSTALLMENT_COLLECTION message [eventUid={}]", eventUid, e);
            sendErrorResponse(eventUid, responseTopic, null, "Malformed message: " + e.getMessage());
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
            byte[] commandBytes = commandSerializer.serialize(command).getBytes(StandardCharsets.UTF_8);

            // Construct RawCommandMessage
            RawCommandMessage rawMessage = new RawCommandMessage(
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    commandBytes,
                    headers,
                    record.timestamp(),
                    responseTopic);

            CommandResponse<?> response = processor.process(rawMessage);

            // Request/Reply: check result and send appropriate response
            if (response.isSuccess()) {
                sendSuccessResponse(message);
            } else {
                LOG.warn(
                        "INSTALLMENT_COLLECTION command failed [eventUid={}, fileNumber={}, status={}]",
                        eventUid,
                        message.fileNumber(),
                        response.httpStatus());
                sendErrorResponse(
                        eventUid,
                        responseTopic,
                        message.fileNumber(),
                        "Command processing failed with status: " + response.httpStatus());
            }

        } catch (Exception e) {
            LOG.error(
                    "Failed to process INSTALLMENT_COLLECTION [eventUid={}, fileNumber={}]",
                    eventUid,
                    message.fileNumber(),
                    e);
            sendErrorResponse(eventUid, responseTopic, message.fileNumber(), e.getMessage());
            throw new RuntimeException("INSTALLMENT_COLLECTION processing failed: " + eventUid, e);
        }
    }

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

    private void sendErrorResponse(String eventUid, String responseTopic, String fileNumber, String errorMessage) {
        if (responseTopic == null || responseTopic.isBlank()) {
            LOG.debug("No responseTopic available [eventUid={}], skipping error reply.", eventUid);
            return;
        }

        InstallmentOperationResponse response = InstallmentOperationResponse.failed(
                eventUid != null ? eventUid : "UNKNOWN",
                OPERATION_TYPE.getCode(),
                fileNumber != null ? fileNumber : "UNKNOWN",
                errorMessage);

        try {
            responsePublisher.sendResponse(responseTopic, response);
        } catch (Exception e) {
            LOG.error("Failed to send error response [eventUid={}]", eventUid, e);
        }
    }
}
