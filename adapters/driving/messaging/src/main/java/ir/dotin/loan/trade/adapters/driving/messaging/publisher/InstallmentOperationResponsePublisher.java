package ir.dotin.loan.trade.adapters.driving.messaging.publisher;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentOperationResponse;
import lombok.RequiredArgsConstructor;

/**
 * Publishes response messages to the corridor response topic as part of
 * the request/reply pattern.
 *
 * <p>The response topic is extracted from the inbound message's
 * {@code responseTopic} field. The {@code eventUid} is used as the
 * Kafka message key for correlation, and the {@code fileNumber} is
 * set as a header for partition-aware routing if needed.</p>
 */
@Component
@RequiredArgsConstructor
public class InstallmentOperationResponsePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(InstallmentOperationResponsePublisher.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Sends a response to the given topic. Non-blocking — logs on success/failure.
     *
     * @param responseTopic  target topic (from inbound message's {@code responseTopic} field)
     * @param response       the response payload
     */
    public void sendResponse(String responseTopic, InstallmentOperationResponse response) {
        byte[] payload;
        try {
            payload = objectMapper.writeValueAsBytes(response);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize response [eventUid={}, topic={}]",
                    response.eventUid(), responseTopic, e);
            return;
        }

        ProducerRecord<String, byte[]> record =
                new ProducerRecord<>(responseTopic, response.eventUid(), payload);

        record.headers()
                .add(new RecordHeader("eventUid",
                        response.eventUid().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("operationType",
                        response.operationType().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("status",
                        response.status().name().getBytes(StandardCharsets.UTF_8)));

        CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(record);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                LOG.error("Failed to publish response [eventUid={}, topic={}]",
                        response.eventUid(), responseTopic, ex);
            } else {
                LOG.info("Response published [eventUid={}, topic={}, partition={}, offset={}]",
                        response.eventUid(), responseTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
