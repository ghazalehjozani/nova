package ir.dotin.loan.trade.adapters.driving.messaging.consumer;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import ir.dotin.platform.adapter.messaging.core.processor.OutboxProcessorMetrics;
import ir.dotin.platform.adapter.messaging.inbox.consumer.AbstractCommandMessageConsumer;
import ir.dotin.platform.adapter.messaging.inbox.exception.InvalidCommandMessageException;
import ir.dotin.platform.adapter.messaging.inbox.handler.CommandMessageHandler;
import ir.dotin.platform.adapter.messaging.inbox.handler.MessagingExceptionHandler;
import ir.dotin.platform.adapter.messaging.inbox.model.CommandHeaders;
import ir.dotin.platform.adapter.messaging.inbox.publisher.CommandResponsePublisher;
import ir.dotin.platform.adapter.messaging.inbox.serializer.InboxSerializer;
import ir.dotin.platform.adapter.messaging.inbox.service.InboxService;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.messaging.handler.FullLoanFacilityLifecycleMessageHandler;

import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TradeLoanCommandMessageConsumer extends AbstractCommandMessageConsumer {

    private static final String PROCESSOR_NAME = "trade-loan-command-consumer";
    private static final String ISSUER_CODE = "LOAN";

    public TradeLoanCommandMessageConsumer(
            InboxService inboxService,
            CommandResponsePublisher responsePublisher,
            OutboxProcessorMetrics metrics,
            List<CommandMessageHandler<?>> handlers,
            MessagingExceptionHandler messagingExceptionHandler,
            JwtDecoder jwtDecoder,
            InboxSerializer inboxSerializer) {
        super(
                inboxService,
                responsePublisher,
                metrics,
                handlers,
                messagingExceptionHandler,
                jwtDecoder,
                inboxSerializer);
    }

    @KafkaListener(
            topics = "corridor.loan.trade.full-lifecycle.command.request.queue.v1",
            groupId = "trade-loan-command-consumer",
            containerFactory = "kafkaListenerContainerFactory")
    @AsyncListener(
            operation =
                    @AsyncOperation(
                            channelName = "corridor.loan.trade.full-lifecycle.command.request.queue.v1",
                            description = "Process trade loan full lifecycle commands",
                            payloadType = FullLoanFacilityLifecycleMessage.class,
                            headers =
                                    @AsyncOperation.Headers(
                                            schemaName = "CommandHeaders",
                                            values = {
                                                @AsyncOperation.Headers.Header(
                                                        name = "X-Request-ID",
                                                        description = "Unique identifier for request",
                                                        value = "Base64 string (UUID)"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "Idempotency-Key",
                                                        description = "Unique identifier for idempotency",
                                                        value = "Base64 string (UUID)"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "X-Request-DateTime",
                                                        description = "Request timestamp",
                                                        value = "Base64 string (ISO-8601)"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "Accept-Language",
                                                        description = "Preferred language",
                                                        value = "string"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "X-Response-Topic",
                                                        description = "Topic for async response",
                                                        value = "Base64 string"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "Authorization",
                                                        description = "Bearer token for authentication",
                                                        value = "Base64 string")
                                            })))
    public void consume(
            @Payload String payload,
            @Header("X-Request-ID") String requestIdBase64,
            @Header("Idempotency-Key") String idempotencyKeyBase64,
            @Header("X-Request-DateTime") String requestDateTimeBase64,
            @Header(value = "Accept-Language", defaultValue = "ZmE=") String acceptLanguageBase64,
            @Header(value = "X-Response-Topic") String responseTopicBase64,
            @Header(value = "Authorization") String authorizationTokenBase64,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack) {

        UnaryOperator<String> decode = str -> {
            if (str == null) return null;
            try {
                return new String(Base64.getDecoder().decode(str));
            } catch (IllegalArgumentException e) {
                return str;
            }
        };

        UUID idempotencyKey = UUID.fromString(decode.apply(idempotencyKeyBase64));
        UUID requestId = UUID.fromString(decode.apply(requestIdBase64));

        Instant requestDateTime = Instant.parse(decode.apply(requestDateTimeBase64));

        String acceptLanguage = decode.apply(acceptLanguageBase64);
        String responseTopic = decode.apply(responseTopicBase64);
        String authorizationToken = decode.apply(authorizationTokenBase64);

        CommandHeaders headers = CommandHeaders.builder()
                .requestId(requestId)
                .idempotencyKey(idempotencyKey)
                .requestDateTime(requestDateTime)
                .acceptLanguage(acceptLanguage)
                .responseTopic(responseTopic)
                .authorizationToken(authorizationToken)
                .build();
        FullLoanFacilityLifecycleMessage deserializedPayload = deserializePayload(payload);

        processMessage(headers, deserializedPayload, topic, ack);
    }

    @Override
    protected String getProcessorName() {
        return PROCESSOR_NAME;
    }

    @Override
    protected String getIssuerCode() {
        return ISSUER_CODE;
    }

    @Override
    protected String resolveCommandType(String topic) {
        return FullLoanFacilityLifecycleMessageHandler.COMMAND_TYPE;
    }

    private FullLoanFacilityLifecycleMessage deserializePayload(String payload) {
        try {
            return inboxSerializer.deserialize(payload, FullLoanFacilityLifecycleMessage.class);
        } catch (Exception e) {
            throw new InvalidCommandMessageException("Failed to deserialize payload", e);
        }
    }
}
