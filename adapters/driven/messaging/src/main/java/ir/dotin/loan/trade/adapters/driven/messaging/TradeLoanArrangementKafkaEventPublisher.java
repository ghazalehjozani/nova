package ir.dotin.loan.trade.adapters.driven.messaging;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.domain.event.DomainEvent;

@Component
public class TradeLoanArrangementKafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TradeLoanArrangementKafkaEventPublisher.class);

    private static final String TOPIC_PREFIX = "loan.trade.arrangement.";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public TradeLoanArrangementKafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(DomainEvent<?, ?> event) {
        try {
            var topic = TOPIC_PREFIX + event.eventType().toLowerCase();
            var key = event.aggregateId().toString();
            var payload = objectMapper.writeValueAsString(event);

            var future = kafkaTemplate.send(topic, key, payload);

            //            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            //                @Override
            //                public void onSuccess(SendResult<String, String> result) {
            //                    log.info("Published event {} to topic {} with key {}", event.eventType(), topic, key);
            //                }
            //
            //                @Override
            //                public void onFailure(Throwable ex) {
            //                    log.error("Failed to publish event {} to topic {}", event.eventType(), topic, ex);
            //                    // Implement retry or DLQ logic
            //                }
            //            });

        } catch (Exception e) {
            log.error("Error publishing event: {}", event, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    public void publishBatch(List<DomainEvent<?, ?>> events) {}
}
