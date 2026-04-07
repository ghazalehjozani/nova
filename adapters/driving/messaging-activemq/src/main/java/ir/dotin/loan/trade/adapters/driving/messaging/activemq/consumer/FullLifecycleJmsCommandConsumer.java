package ir.dotin.loan.trade.adapters.driving.messaging.activemq.consumer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import ir.dotin.platform.messaging.activemq.converter.JmsInboundMessageConverter;
import ir.dotin.platform.messaging.api.command.CommandResponse;
import ir.dotin.platform.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.messaging.api.outbound.spi.ResponsePublisher;
import ir.dotin.platform.messaging.core.processor.InboundCommandProcessor;
import ir.dotin.platform.messaging.core.serialization.CommandSerializer;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.FullLoanFacilityLifecycleMessageMapper;
import ir.dotin.loan.trade.adapters.driving.messaging.activemq.config.ActiveMqJmsConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FullLifecycleJmsCommandConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(FullLifecycleJmsCommandConsumer.class);

    private static final String OPERATION_TYPE = "FULL_LOAN_FACILITY_LIFECYCLE";

    private final ObjectMapper objectMapper;
    private final FullLoanFacilityLifecycleMessageMapper messageMapper;
    private final InboundCommandProcessor inboundCommandProcessor;
    private final CommandSerializer commandSerializer;
    private final JmsInboundMessageConverter jmsConverter;
    private final ResponsePublisher jmsResponsePublisher;

    @JmsListener(destination = ActiveMqJmsConfig.FULL_LIFECYCLE_QUEUE, containerFactory = "jmsListenerContainerFactory")
    public void consume(
            Message jmsMessage,
            @Payload String body,
            @Header(value = "Idempotency-Key") String idempotencyKey,
            @Header(value = "X-Correlation-ID") String correlationId,
            @Header(value = "X-Request-DateTime") String requestDateTime,
            @Header(value = "Accept-Language") String acceptLanguage,
            @Header(value = "Authorization") String authorization,
            @Header(value = "traceparent") String traceparent,
            @Header(value = "tracestate", required = false) String tracestate,
            @Header(value = "X-Saga-Correlation-ID") String sagaCorrelationId,
            @Header(value = "X-Saga-Execution-Strategy") String sagaExecutionStrategy,
            @Header(value = "X-Saga-Step-Code", required = false) String sagaStepCode)
            throws JMSException {
        try {
            InboundMessage inboundMessage = jmsConverter.convert(jmsMessage, ActiveMqJmsConfig.FULL_LIFECYCLE_QUEUE);
            String correlationKey = inboundMessage.correlationKey();

            FullLoanFacilityLifecycleMessage message =
                    objectMapper.readValue(body, FullLoanFacilityLifecycleMessage.class);

            FullLoanFacilityLifecycleCommand command = messageMapper.toCommand(message).toBuilder()
                    .uid(UUID.randomUUID())
                    .transactionMetadata(buildTransactionMetadata(jmsMessage))
                    .build();

            byte[] commandBytes = commandSerializer.serialize(command).getBytes(StandardCharsets.UTF_8);

            CommandResponse<?> response = inboundCommandProcessor.process(inboundMessage.withPayload(commandBytes));

            // Publish protocol-compliant response via ResponsePublisher
            String responseDestination = inboundMessage.responseDestination();
            if (responseDestination != null && !responseDestination.isBlank()) {
                jmsResponsePublisher.publish(responseDestination, correlationKey, response);
            }

            LOG.info("JMS command processed successfully [correlationId={}]", correlationId);
        } catch (Exception e) {
            LOG.error("Failed to process JMS command [correlationId={}]: {}", correlationId, e.getMessage(), e);
            throw new RuntimeException("JMS command processing failed [correlationId=" + correlationId + "]", e);
        } finally {
            jmsMessage.acknowledge();
        }
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
