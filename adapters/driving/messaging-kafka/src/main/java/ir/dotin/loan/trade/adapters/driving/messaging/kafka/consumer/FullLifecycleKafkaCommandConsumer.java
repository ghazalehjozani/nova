package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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

    @KafkaListener(
            topics = "corridor.core.loan.nova.full-lifecycle.request.queue.v1",
            groupId = "core.loan.facility.*",
            containerFactory = "byteArrayKafkaListenerContainerFactory")
    @AsyncListener(
            operation =
                    @AsyncOperation(
                            channelName = "corridor.core.loan.nova.full-lifecycle.request.queue.v1",
                            description = "Process nova loan full lifecycle commands",
                            headers =
                                    @AsyncOperation.Headers(
                                            schemaName = "CommandHeaders",
                                            values = {
                                                @AsyncOperation.Headers.Header(
                                                        name = "Idempotency-Key",
                                                        description = "Unique identifier for idempotency",
                                                        value = "UUID string"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "X-Correlation-ID",
                                                        description = "Unique identifier for correlation ID",
                                                        value = "string"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "X-Request-DateTime",
                                                        description = "Request timestamp",
                                                        value = "ISO-8601 format"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "Accept-Language",
                                                        description = "Preferred language",
                                                        value = "Language code"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "Authorization",
                                                        description = "Bearer token for authentication",
                                                        value = "Bearer token"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "traceparent",
                                                        description = "W3C trace context",
                                                        value = "Trace parent ID"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "tracestate",
                                                        description = "W3C trace state",
                                                        value = "Trace state"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "X-Saga-Correlation-ID",
                                                        description = "Correlation ID for saga orchestration.",
                                                        value = "uuid-string"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "X-Saga-Step-Code",
                                                        description =
                                                                "Identifier for the step for breakpoint in the saga workflow.",
                                                        value = "step-identifier"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "X-Saga-Execution-Strategy",
                                                        description = "Strategy for handling saga failures.",
                                                        value = "ROLLBACK_ALL | STOP_ON_STEP")
                                            })))
    public void consume(ConsumerRecord<String, byte[]> consumerRecord) {
        try {
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

            CommandResponse<Object> response =
                    inboundCommandProcessor.process(inboundMessage.withPayload(commandBytes));

            String responseDestination = inboundMessage.responseDestination();
            if (responseDestination != null && !responseDestination.isBlank()) {
                kafkaResponsePublisher.publish(responseDestination, inboundMessage.correlationKey(), response);
            }

        } catch (Exception e) {
            LOG.error("Failed to process full lifecycle command [key={}]: {}", consumerRecord.key(), e.getMessage(), e);
            throw new RuntimeException("Full lifecycle command processing failed", e);
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
