package ir.dotin.loan.trade.adapters.driving.messaging.activemq.consumer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import ir.dotin.platform.adapter.messaging.command.model.CommandResponse;
import ir.dotin.platform.adapter.messaging.command.model.RawCommandMessage;
import ir.dotin.platform.adapter.messaging.command.processor.CommandProcessor;
import ir.dotin.platform.adapter.messaging.command.serializer.CommandSerializer;
import ir.dotin.loan.trade.adapters.driving.messaging.activemq.dto.JmsCommandReply;
import ir.dotin.loan.trade.adapters.driving.messaging.activemq.publisher.JmsReplyPublisher;
import ir.dotin.loan.trade.adapters.driving.messaging.activemq.support.JmsRawCommandMessageFactory;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.messaging.mapper.FullLoanFacilityLifecycleMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FullLifecycleJmsCommandConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(FullLifecycleJmsCommandConsumer.class);

    private static final String OPERATION_TYPE = "FULL_LOAN_FACILITY_LIFECYCLE";
    private static final String DESTINATION = "corridor.core.loan.nova.full-lifecycle.request.queue.v1";

    private final ObjectMapper objectMapper;
    private final FullLoanFacilityLifecycleMessageMapper messageMapper;
    private final CommandProcessor processor;
    private final CommandSerializer commandSerializer;
    private final JmsReplyPublisher replyPublisher;

    // TODO: should set and use this headers, explicit set is recommenced
    @JmsListener(destination = DESTINATION, containerFactory = "jmsListenerContainerFactory")
    public void consume(
            Message jmsMessage,
            @Payload String body,
            @Header(value = "X-Request-ID") String requestId,
            @Header(value = "Idempotency-Key") String idempotencyKey,
            @Header(value = "X-Request-DateTime") String requestDateTime,
            @Header(value = "X-Response-Topic") String responseTopic,
            @Header(value = "Accept-Language") String acceptLanguage,
            @Header(value = "X-Saga-Correlation-ID") String sagaCorrelationId,
            @Header(value = "X-Saga-Execution-Strategy") String sagaExecutionStrategy,
            @Header(value = "X-Saga-Step-Code", required = false) String sagaStepCode,
            @Header(value = "correlation-id") String correlationId,
            @Header(value = "Authorization") String authorization)
            throws JMSException {
        Destination replyTo = null;

        try {
            correlationId = resolveCorrelationId(jmsMessage);
            replyTo = jmsMessage.getJMSReplyTo();

            LOG.info("Received JMS command [correlationId={}, destination={}]", correlationId, DESTINATION);

            JsonNode rootNode = objectMapper.readTree(body);

            // ── Deserialise & map ──────────────────────────────────
            FullLoanFacilityLifecycleMessage message =
                    objectMapper.treeToValue(rootNode, FullLoanFacilityLifecycleMessage.class);

            FullLoanFacilityLifecycleCommand command = messageMapper.toCommand(message).toBuilder()
                    .uid(UUID.randomUUID())
                    .transactionMetadata(buildTransactionMetadata(jmsMessage))
                    .build();

            byte[] commandBytes = commandSerializer.serialize(command).getBytes(StandardCharsets.UTF_8);

            // ── Delegate to platform command pipeline ──────────────
            RawCommandMessage rawMessage = JmsRawCommandMessageFactory.from(jmsMessage, DESTINATION);

            // TODO: should set commandResponse in response and response should have standard with BaseResponse or
            // ErrorResponse in protocol module
            CommandResponse<Object> commandResponse = processor.process(
                    rawMessage.toBuilder().payload(commandBytes).build());

            // ── Reply: success ─────────────────────────────────────
            JmsCommandReply successReply = JmsCommandReply.success(correlationId, OPERATION_TYPE);
            replyPublisher.sendReply(replyTo, responseTopic, correlationId, successReply);

            LOG.info("JMS command processed successfully [correlationId={}]", correlationId);

        } catch (Exception e) {
            LOG.error("Failed to process JMS command [correlationId={}]: {}", correlationId, e.getMessage(), e);

            JmsCommandReply errorReply = JmsCommandReply.failed(
                    correlationId,
                    OPERATION_TYPE,
                    "LOAN-0500",
                    e.getMessage()); // TODO: this is wrong, should use standard codes
            replyPublisher.sendReply(replyTo, responseTopic, correlationId, errorReply);

            throw new RuntimeException("JMS command processing failed [correlationId=" + correlationId + "]", e);
        } finally {
            jmsMessage.acknowledge();
        }
    }

    // ── Private helpers ──────────────────────────────────────────────

    private String resolveCorrelationId(Message message) throws JMSException {
        String id = message.getJMSCorrelationID();
        if (id != null && !id.isBlank()) {
            return id;
        }
        // Fall back to JMSMessageID, then generate a UUID
        String messageId = message.getJMSMessageID();
        if (messageId != null && !messageId.isBlank()) {
            return messageId;
        }
        return UUID.randomUUID().toString();
    }

    private String extractStringField(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        return (node != null && !node.isNull()) ? node.asText() : null;
    }

    private FullLoanFacilityLifecycleCommand.TransactionMetadataDto buildTransactionMetadata(Message message)
            throws JMSException {

        return FullLoanFacilityLifecycleCommand.TransactionMetadataDto.builder()
                .branchCode(getStringProperty(message, "branchCode", "1"))
                .userId(getStringProperty(message, "userId", "SYSTEM"))
                .terminalId(getStringProperty(message, "terminalId", "ACTIVEMQ"))
                .terminalIp(getStringProperty(message, "terminalIp", "0.0.0.0"))
                .terminalType(getStringProperty(message, "terminalType", "MESSAGING"))
                .channel("ACTIVEMQ")
                .toolSource("NOVA")
                .productCode("TRADE_LOAN")
                .networkType("INTERNAL")
                .build();
    }

    private String getStringProperty(Message message, String name, String defaultValue) throws JMSException {
        String value = message.getStringProperty(name);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
