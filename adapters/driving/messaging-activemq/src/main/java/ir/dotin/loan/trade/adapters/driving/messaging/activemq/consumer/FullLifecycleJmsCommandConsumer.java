package ir.dotin.loan.trade.adapters.driving.messaging.activemq.consumer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.messaging.activemq.converter.JmsInboundMessageConverter;
import ir.dotin.platform.pangaea.messaging.api.command.CommandResponse;
import ir.dotin.platform.pangaea.messaging.api.header.MessagingHeaderNames;
import ir.dotin.platform.pangaea.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.pangaea.messaging.api.inbound.InboundMessageHeaders;
import ir.dotin.platform.pangaea.messaging.api.outbound.spi.ResponsePublisher;
import ir.dotin.platform.pangaea.messaging.core.processor.InboundCommandProcessor;
import ir.dotin.platform.pangaea.messaging.core.serialization.CommandSerializer;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.FullLoanFacilityLifecycleMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;

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
    private final JmsInboundMessageConverter converter;
    private final ResponsePublisher jmsResponsePublisher;

    @JmsListener(
            destination = "corridor.core.loan.nova.full-lifecycle.request.queue.v1",
            containerFactory = "jmsListenerContainerFactory")
    public void consume(Message message) {
        if (!(message instanceof TextMessage textMessage)) {
            throw new IllegalArgumentException(
                    "Unsupported JMS message type: " + message.getClass().getSimpleName());
        }

        InboundMessage inboundMessage = converter.convert(textMessage);
        validateRequiredHeaders(inboundMessage.headers());

        FullLoanFacilityLifecycleMessage payload =
                objectMapper.readValue(inboundMessage.payload(), FullLoanFacilityLifecycleMessage.class);

        FullLoanFacilityLifecycleCommand command = messageMapper.toCommand(payload).toBuilder()
                .uid(UUID.randomUUID())
                .transactionMetadata(buildDefaultTransactionMetadata())
                .build();

        byte[] commandBytes = commandSerializer.serialize(command).getBytes(StandardCharsets.UTF_8);

        CommandResponse<Object> response = inboundCommandProcessor.process(inboundMessage.withPayload(commandBytes));

        String responseDestination = inboundMessage.responseDestination();
        if (responseDestination != null && !responseDestination.isBlank()) {
            jmsResponsePublisher.publish(responseDestination, inboundMessage.correlationKey(), response);
        }
    }

    private FullLoanFacilityLifecycleCommand.TransactionMetadataDto buildDefaultTransactionMetadata() {
        return FullLoanFacilityLifecycleCommand.TransactionMetadataDto.builder()
                .branchCode("1")
                .userId("SYSTEM")
                .terminalId("ACTIVEMQ")
                .terminalIp("0.0.0.0")
                .terminalType("MESSAGING")
                .channel("ACTIVEMQ")
                .toolSource("NOVA")
                .productCode("TRADE_LOAN")
                .networkType("INTERNAL")
                .build();
    }

    private void validateRequiredHeaders(InboundMessageHeaders headers) {
        if (headers.idempotencyKey() == null) {
            throw new IllegalArgumentException("Missing required header: " + MessagingHeaderNames.IDEMPOTENCY_KEY);
        }
        if (headers.requestDateTime() == null) {
            throw new IllegalArgumentException("Missing required header: " + MessagingHeaderNames.REQUEST_DATETIME);
        }
        if (headers.acceptLanguage() == null) {
            throw new IllegalArgumentException("Missing required header: " + MessagingHeaderNames.ACCEPT_LANGUAGE);
        }
        if (headers.authorizationToken() == null) {
            throw new IllegalArgumentException("Missing required header: " + MessagingHeaderNames.AUTHORIZATION);
        }
        if (headers.traceparent() == null) {
            throw new IllegalArgumentException("Missing required header: " + MessagingHeaderNames.TRACEPARENT);
        }
        if (headers.correlationId() == null) {
            throw new IllegalArgumentException("Missing required header: " + MessagingHeaderNames.CORRELATION_ID);
        }
    }
}
