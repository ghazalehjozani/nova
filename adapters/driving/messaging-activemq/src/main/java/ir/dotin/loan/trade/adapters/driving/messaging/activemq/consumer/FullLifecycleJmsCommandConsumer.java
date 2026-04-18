package ir.dotin.loan.trade.adapters.driving.messaging.activemq.consumer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import ir.dotin.platform.asyncapi.header.MessagingHeaderNames;
import ir.dotin.platform.messaging.activemq.converter.JmsInboundMessageConverter;
import ir.dotin.platform.messaging.activemq.support.JmsDestinationResolver;
import ir.dotin.platform.messaging.api.command.CommandResponse;
import ir.dotin.platform.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.messaging.api.outbound.spi.ResponsePublisher;
import ir.dotin.platform.messaging.core.processor.InboundCommandProcessor;
import ir.dotin.platform.messaging.core.serialization.CommandSerializer;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.FullLoanFacilityLifecycleMessageMapper;
import ir.dotin.loan.trade.adapters.driving.messaging.activemq.config.ActiveMqJmsConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;

import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class FullLifecycleJmsCommandConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(FullLifecycleJmsCommandConsumer.class);

    private final ObjectMapper objectMapper;
    private final FullLoanFacilityLifecycleMessageMapper messageMapper;
    private final InboundCommandProcessor inboundCommandProcessor;
    private final CommandSerializer commandSerializer;
    private final JmsInboundMessageConverter jmsConverter;
    private final ResponsePublisher jmsResponsePublisher;

    @AsyncListener(
            operation =
                    @AsyncOperation(
                            channelName = ActiveMqJmsConfig.FULL_LIFECYCLE_QUEUE,
                            description =
                                    "Process nova loan full lifecycle command (ActiveMQ, saga-driven, request/reply).",
                            servers = "activemq",
                            headers =
                                    @AsyncOperation.Headers(
                                            schemaName = MessagingHeaderNames.SCHEMA_SAGA_COMMAND_HEADERS)))
    @JmsListener(
            destination = ActiveMqJmsConfig.FULL_LIFECYCLE_QUEUE,
            subscription = "${platform.messaging.kafka.consumer-group-id}",
            containerFactory = "jmsListenerContainerFactory")
    public void consume(Message jmsMessage) throws JMSException {
        String correlationKey = JmsDestinationResolver.resolveCorrelationId(jmsMessage);
        try {
            InboundMessage inboundMessage = jmsConverter.convert(jmsMessage, ActiveMqJmsConfig.FULL_LIFECYCLE_QUEUE);

            FullLoanFacilityLifecycleMessage message =
                    objectMapper.readValue(jmsMessage.getBody(String.class), FullLoanFacilityLifecycleMessage.class);

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

            LOG.info("JMS command processed successfully [correlationId={}]", correlationKey);
        } catch (Exception e) {
            LOG.error("Failed to process JMS command [correlationId={}]: {}", correlationKey, e.getMessage(), e);
            throw new RuntimeException("JMS command processing failed [correlationId=" + correlationKey + "]", e);
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
