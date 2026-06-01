package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.messaging.api.command.CommandResponse;
import ir.dotin.platform.pangaea.messaging.api.header.MessagingHeaderNames;
import ir.dotin.platform.pangaea.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.pangaea.messaging.api.inbound.InboundMessageHeaders;
import ir.dotin.platform.pangaea.messaging.api.outbound.spi.ResponsePublisher;
import ir.dotin.platform.pangaea.messaging.core.processor.InboundCommandProcessor;
import ir.dotin.platform.pangaea.messaging.core.serialization.CommandSerializer;
import ir.dotin.platform.pangaea.messaging.kafka.converter.KafkaInboundMessageConverter;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.FullLoanFacilityLifecycleMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * @deprecated superseded by the Artemis FCB corridor. The full-lifecycle request/reply ingress is no longer part of the
 *     active topology — the Nova ↔ FCB request/reply path now rides the pluggable {@code FcbRequestReplyClient} seam
 *     routed in {@code container}. The bean only registers when
 *     {@code nova.driving.messaging-kafka.full-lifecycle.enabled=true} (absent ⇒ OFF), so by default its
 *     {@code @KafkaListener} never joins the consumer group. Class kept for reference / a deliberate re-enable; do not
 *     build new flows on it.
 */
@Deprecated
@Component
@ConditionalOnProperty(prefix = "nova.driving.messaging-kafka.full-lifecycle", name = "enabled", matchIfMissing = false)
@RequiredArgsConstructor
public class FullLifecycleKafkaCommandConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(FullLifecycleKafkaCommandConsumer.class);

    private final ObjectMapper objectMapper;
    private final FullLoanFacilityLifecycleMessageMapper messageMapper;
    private final InboundCommandProcessor inboundCommandProcessor;
    private final CommandSerializer commandSerializer;
    private final KafkaInboundMessageConverter converter;
    private final ResponsePublisher kafkaResponsePublisher;

    @KafkaListener(
            topics = "corridor.core.loan.nova.full-lifecycle.request.queue.v1",
            groupId = "${platform.messaging.kafka.consumer-group-id}",
            containerFactory = "byteArrayKafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, byte[]> consumerRecord) {
        InboundMessage inboundMessage = converter.convert(consumerRecord);
        validateRequiredHeaders(inboundMessage.headers());

        FullLoanFacilityLifecycleMessage message =
                objectMapper.readValue(inboundMessage.payload(), FullLoanFacilityLifecycleMessage.class);

        FullLoanFacilityLifecycleCommand command = messageMapper.toCommand(message).toBuilder()
                .uid(UUID.randomUUID())
                // FIXME: transactionMetadata should be extracted from message headers/body
                // when the upstream system provides it. Using defaults as fallback.
                .transactionMetadata(buildDefaultTransactionMetadata())
                .build();

        byte[] commandBytes = commandSerializer.serialize(command).getBytes(StandardCharsets.UTF_8);

        CommandResponse<Object> response = inboundCommandProcessor.process(inboundMessage.withPayload(commandBytes));

        String responseDestination = inboundMessage.responseDestination();
        if (responseDestination != null && !responseDestination.isBlank()) {
            kafkaResponsePublisher.publish(responseDestination, inboundMessage.correlationKey(), response);
        }
    }

    private FullLoanFacilityLifecycleCommand.TransactionMetadataDto buildDefaultTransactionMetadata() {
        return FullLoanFacilityLifecycleCommand.TransactionMetadataDto.builder()
                .branchCode("1")
                .userId("SYSTEM")
                .terminalId("KAFKA")
                .terminalIp("0.0.0.0")
                .terminalType("MESSAGING")
                .channel("KAFKA")
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
