package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.messaging.api.inbound.InboundMessageHeaders;
import ir.dotin.platform.messaging.core.processor.InboundCommandProcessor;
import ir.dotin.platform.messaging.core.serialization.CommandSerializer;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;
import ir.dotin.loan.trade.adapters.driving.contract.dto.InstallmentCollectionCompensateMessage;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollectInstallmentCommand;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstallmentCollectionCompensateHandler implements FcbEventOperationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(InstallmentCollectionCompensateHandler.class);
    private static final FcbEventOperationType OPERATION_TYPE = FcbEventOperationType.INSTALLMENT_COLLECTION_COMPENSATE;

    private final ObjectMapper objectMapper;
    private final InboundCommandProcessor inboundCommandProcessor;
    private final CommandSerializer commandSerializer;

    @Override
    public FcbEventOperationType getSupportedOperationType() {
        return OPERATION_TYPE;
    }

    @Override
    public void handle(JsonNode rootNode, InboundMessageHeaders headers, InboundMessage message, String eventUid) {
        InstallmentCollectionCompensateMessage compensateMessage;
        try {
            compensateMessage = objectMapper.treeToValue(rootNode, InstallmentCollectionCompensateMessage.class);
        } catch (Exception e) {
            LOG.error("Malformed INSTALLMENT_COLLECTION_COMPENSATE message [eventUid={}]", eventUid, e);
            return;
        }

        LOG.info(
                "Processing INSTALLMENT_COLLECTION_COMPENSATE [eventUid={}, fileNumber={}]",
                eventUid,
                compensateMessage.fileNumber());

        try {
            CompensateCollectInstallmentCommand command = CompensateCollectInstallmentCommand.builder()
                    .uid(UUID.fromString(compensateMessage.eventUid()))
                    .applicationNumber(compensateMessage.fileNumber())
                    .transactionNumbers(compensateMessage.transactionNumbers())
                    .build();

            byte[] commandBytes = commandSerializer.serialize(command).getBytes(StandardCharsets.UTF_8);

            inboundCommandProcessor.process(message.withPayload(commandBytes));

        } catch (Exception e) {
            LOG.error(
                    "Failed to process INSTALLMENT_COLLECTION_COMPENSATE [eventUid={}, fileNumber={}]",
                    eventUid,
                    compensateMessage.fileNumber(),
                    e);
            throw new RuntimeException("INSTALLMENT_COLLECTION_COMPENSATE processing failed: " + eventUid, e);
        }
    }
}
