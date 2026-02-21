package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import org.apache.kafka.clients.producer.Producer;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("kafka-fcb")
@RequiredArgsConstructor
public class FcbKafkaHealthIndicator implements HealthIndicator {

    private final KafkaTemplate<String, byte[]> byteArrayKafkaTemplate;
    private final FcbKafkaProperties properties;

    @Override
    public Health health() {
        try (Producer<String, byte[]> producer =
                byteArrayKafkaTemplate.getProducerFactory().createProducer()) {
            producer.partitionsFor(properties.requestTopic());
            return Health.up()
                    .withDetail("requestTopic", properties.requestTopic())
                    .withDetail("replyTopic", properties.replyTopic())
                    .build();
        } catch (Exception e) {
            log.error("Kafka broker health check failed", e);
            return Health.down()
                    .withDetail("requestTopic", properties.requestTopic())
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
