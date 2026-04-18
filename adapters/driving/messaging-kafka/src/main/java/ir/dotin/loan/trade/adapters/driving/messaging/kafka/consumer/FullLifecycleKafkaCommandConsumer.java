package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ir.dotin.platform.asyncapi.header.MessagingHeaderNames;
import ir.dotin.platform.messaging.api.command.CommandResponse;
import ir.dotin.platform.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.messaging.api.inbound.InboundMessageHeaders;
import ir.dotin.platform.messaging.api.outbound.spi.ResponsePublisher;
import ir.dotin.platform.messaging.core.processor.InboundCommandProcessor;
import ir.dotin.platform.messaging.core.serialization.CommandSerializer;
import ir.dotin.platform.messaging.kafka.converter.KafkaInboundMessageConverter;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.FullLoanFacilityLifecycleMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;

import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class FullLifecycleKafkaCommandConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(FullLifecycleKafkaCommandConsumer.class);

    private final ObjectMapper objectMapper;
    private final FullLoanFacilityLifecycleMessageMapper messageMapper;
    private final InboundCommandProcessor inboundCommandProcessor;
    private final CommandSerializer commandSerializer;
    private final KafkaInboundMessageConverter converter;
    private final ResponsePublisher kafkaResponsePublisher;

    @AsyncListener(
            operation =
                    @AsyncOperation(
                            channelName = "corridor.core.loan.nova.full-lifecycle.request.queue.v1",
                            description =
                                    "Process nova loan full lifecycle commands (Kafka, saga-driven, request/reply).",
                            servers = "kafka",
                            headers =
                                    @AsyncOperation.Headers(
                                            schemaName = MessagingHeaderNames.SCHEMA_SAGA_COMMAND_HEADERS)))
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

        // Re-serialize with polymorphic type info for the command pipeline.
        // This is required because the pipeline deserializes the byte payload back to a
        // Command instance using type information. Ideally the pipeline would accept a
        // pre-built Command directly — consider adding such an overload.
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
            throw new IllegalArgumentException("Missing required header: Idempotency-Key");
        }
        if (headers.requestDateTime() == null) {
            throw new IllegalArgumentException("Missing required header: X-Request-DateTime");
        }
        if (headers.acceptLanguage() == null) {
            throw new IllegalArgumentException("Missing required header: Accept-Language");
        }
        if (headers.authorizationToken() == null) {
            throw new IllegalArgumentException("Missing required header: Authorization");
        }
        if (headers.traceparent() == null) {
            throw new IllegalArgumentException("Missing required header: traceparent");
        }
        // Saga headers required for full lifecycle
        if (headers.sagaCorrelationId() == null) {
            throw new IllegalArgumentException("Missing required header: X-Saga-Correlation-ID");
        }
        if (headers.sagaExecutionStrategy() == null) {
            throw new IllegalArgumentException("Missing required header: X-Saga-Execution-Strategy");
        }
        // tracestate and sagaStepCode are optional
    }
}
