package ir.dotin.loan.trade.adapters.driving.messaging.consumer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ir.dotin.platform.adapter.messaging.command.model.CommandHeaders;
import ir.dotin.platform.adapter.messaging.command.model.RawCommandMessage;
import ir.dotin.platform.adapter.messaging.command.processor.CommandProcessor;
import ir.dotin.platform.adapter.messaging.command.serializer.CommandSerializer;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.messaging.mapper.FullLoanFacilityLifecycleMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;

import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TradeLoanCommandConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TradeLoanCommandConsumer.class);

    private final ObjectMapper objectMapper;
    private final FullLoanFacilityLifecycleMessageMapper messageMapper;
    private final CommandProcessor processor;
    private final CommandSerializer commandSerializer;

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
                                                        value = "Trace state")
                                            })))
    public void consume(ConsumerRecord<String, byte[]> consumerRecord) {
        try {
            JsonNode rootNode = objectMapper.readTree(consumerRecord.value());

            String authorization = getText(rootNode, "authorization", null);
            if (authorization == null) {
                authorization = getHeader(consumerRecord, "Authorization");
            }
            String idempotencyKey = getHeader(consumerRecord, "Idempotency-Key");
            String requestDateTime = getHeader(consumerRecord, "X-Request-DateTime");

            CommandHeaders headers = CommandHeaders.builder()
                    .authorizationToken(authorization)
                    .idempotencyKey(idempotencyKey)
                    .requestDateTime(requestDateTime)
                    .acceptLanguage(getHeader(consumerRecord, "Accept-Language"))
                    .traceparent(getHeader(consumerRecord, "traceparent"))
                    .tracestate(getHeader(consumerRecord, "tracestate"))
                    .build();

            FullLoanFacilityLifecycleMessage message =
                    objectMapper.treeToValue(rootNode, FullLoanFacilityLifecycleMessage.class);

            FullLoanFacilityLifecycleCommand command = messageMapper.toCommand(message).toBuilder()
                    .uid(UUID.randomUUID())
                    .transactionMetadata(buildDefaultTransactionMetadata()) // TODO: Fix
                    .build();

            byte[] commandBytes = commandSerializer.serialize(command).getBytes(StandardCharsets.UTF_8);
            RawCommandMessage rawMessage = new RawCommandMessage(
                    consumerRecord.topic(),
                    consumerRecord.partition(),
                    consumerRecord.offset(),
                    consumerRecord.key(),
                    commandBytes,
                    headers,
                    consumerRecord.timestamp(),
                    null);
            processor.process(rawMessage);

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

    private String getText(JsonNode node, String fieldName, String defaultValue) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return defaultValue;
    }

    private String getHeader(ConsumerRecord<String, byte[]> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (header == null || header.value() == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
