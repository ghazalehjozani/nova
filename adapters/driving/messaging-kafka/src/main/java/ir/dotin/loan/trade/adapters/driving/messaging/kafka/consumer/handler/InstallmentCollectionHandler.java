package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler;

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
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentOperationType;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentPaymentMessage;
import ir.dotin.loan.trade.adapters.driving.messaging.mapper.InstallmentCollectionMessageMapper;
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

        } catch (Exception e) {
            LOG.error(
                    "Failed to process INSTALLMENT_COLLECTION [eventUid={}, fileNumber={}]",
                    eventUid,
                    message.fileNumber(),
                    e);
            throw new RuntimeException("INSTALLMENT_COLLECTION processing failed: " + eventUid, e);
        }
    }
}
