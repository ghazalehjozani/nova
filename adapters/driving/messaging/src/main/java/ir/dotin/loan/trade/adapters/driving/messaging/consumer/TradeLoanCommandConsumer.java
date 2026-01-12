package ir.dotin.loan.trade.adapters.driving.messaging.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ir.dotin.platform.adapter.messaging.command.model.RawCommandMessage;
import ir.dotin.platform.adapter.messaging.command.processor.CommandProcessor;

import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TradeLoanCommandConsumer {

    private final CommandProcessor processor;

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
        RawCommandMessage rawMessage = RawCommandMessage.from(consumerRecord);
        processor.process(rawMessage);
    }
}
