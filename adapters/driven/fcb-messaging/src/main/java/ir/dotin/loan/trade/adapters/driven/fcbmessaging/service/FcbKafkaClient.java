package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbKafkaLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.KafkaErrorCodeMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@Profile("kafka-fcb")
@RequiredArgsConstructor
public class FcbKafkaClient {

    private final ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FcbKafkaProperties properties;

    public Result<FcbKafkaBaseResponse> sendAndReceive(FcbKafkaBaseRequest request, Duration timeout) {
        log.debug(
                "Sending Kafka request: operation={}, eventUid={}",
                request.getOperationName(),
                request.getEventUid());

        try {
            byte[] requestBytes = objectMapper.writeValueAsBytes(request);

            ProducerRecord<String, byte[]> record =
                    new ProducerRecord<>(properties.requestTopic(), request.getEventUid(), requestBytes);
            record.headers()
                    .add(new RecordHeader(
                            "operationName",
                            request.getOperationName().getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(
                            "eventUid",
                            request.getEventUid().getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader(
                            "Idempotency-Key",
                            request.getEventUid().getBytes(StandardCharsets.UTF_8)));

            RequestReplyFuture<String, byte[], byte[]> future =
                    replyingKafkaTemplate.sendAndReceive(record, timeout);

            ConsumerRecord<String, byte[]> replyRecord = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);

            if (replyRecord.value() == null || replyRecord.value().length == 0) {
                log.warn("Empty Kafka reply for operation={}", request.getOperationName());
                return Result.failure(Notification.ofError(
                        FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE,
                        request.getOperationName()));
            }

            FcbKafkaBaseResponse response =
                    objectMapper.readValue(replyRecord.value(), FcbKafkaBaseResponse.class);

            if (response.isError()) {
                log.warn(
                        "Kafka FCB error response: operation={}, errorCode={}, errorMessage={}",
                        request.getOperationName(),
                        response.getErrorCode(),
                        response.getErrorMessage());
                return Result.failure(KafkaErrorCodeMapper.mapToNotification(response));
            }

            log.debug(
                    "Kafka reply received: operation={}, correlationId={}",
                    request.getOperationName(),
                    response.getCorrelationId());
            return Result.success(response);

        } catch (java.util.concurrent.TimeoutException e) {
            log.warn(
                    "Kafka reply timed out: operation={}, timeout={}ms",
                    request.getOperationName(),
                    timeout.toMillis());
            return Result.failure(Notification.ofError(
                    FcbKafkaLocalizedMessageCodes.KAFKA_REPLY_TIMEOUT,
                    request.getOperationName(),
                    String.valueOf(timeout.toMillis())));
        } catch (Exception e) {
            log.error("Kafka communication error: operation={}", request.getOperationName(), e);
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_COMMUNICATION_ERROR, e.getMessage()));
        }
    }
}
