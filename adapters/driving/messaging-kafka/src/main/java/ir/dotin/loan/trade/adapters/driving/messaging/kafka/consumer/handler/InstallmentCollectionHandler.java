package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.messaging.api.inbound.InboundMessageHeaders;
import ir.dotin.platform.messaging.core.processor.InboundCommandProcessor;
import ir.dotin.platform.messaging.core.serialization.CommandSerializer;
import ir.dotin.loan.trade.adapters.driving.contract.dto.InstallmentOperationType;
import ir.dotin.loan.trade.adapters.driving.contract.dto.InstallmentPaymentMessage;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.InstallmentCollectionMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstallmentCollectionHandler implements InstallmentOperationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(InstallmentCollectionHandler.class);
    private static final InstallmentOperationType OPERATION_TYPE = InstallmentOperationType.INSTALLMENT_COLLECTION;

    private final ObjectMapper objectMapper;
    private final InstallmentCollectionMessageMapper messageMapper;
    private final InboundCommandProcessor inboundCommandProcessor;
    private final CommandSerializer commandSerializer;

    @Override
    public InstallmentOperationType getSupportedOperationType() {
        return OPERATION_TYPE;
    }

    @Override
    public void handle(JsonNode rootNode, InboundMessageHeaders headers, InboundMessage message, String eventUid) {
        InstallmentPaymentMessage paymentMessage;
        try {
            paymentMessage = objectMapper.treeToValue(rootNode, InstallmentPaymentMessage.class);
        } catch (Exception e) {
            LOG.error("Malformed INSTALLMENT_COLLECTION message [eventUid={}]", eventUid, e);
            return;
        }

        LOG.info(
                "Processing INSTALLMENT_COLLECTION [eventUid={}, fileNumber={}, payments={}]",
                eventUid,
                paymentMessage.fileNumber(),
                paymentMessage.payments() != null ? paymentMessage.payments().size() : 0);

        try {
            CollectInstallmentCommand command = messageMapper.toCommand(paymentMessage);

            byte[] commandBytes = commandSerializer.serialize(command).getBytes(StandardCharsets.UTF_8);

            inboundCommandProcessor.process(message.withPayload(commandBytes));

        } catch (Exception e) {
            LOG.error(
                    "Failed to process INSTALLMENT_COLLECTION [eventUid={}, fileNumber={}]",
                    eventUid,
                    paymentMessage.fileNumber(),
                    e);
            throw new RuntimeException("INSTALLMENT_COLLECTION processing failed: " + eventUid, e);
        }
    }
}
