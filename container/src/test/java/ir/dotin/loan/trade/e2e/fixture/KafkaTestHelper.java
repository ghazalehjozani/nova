package ir.dotin.loan.trade.e2e.fixture;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

public final class KafkaTestHelper {

    private KafkaTestHelper() {}

    public static ProducerRecord<String, byte[]> buildRecord(
            String topic, String key, byte[] payload, String authToken, String operationType, String eventUid) {

        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, payload);

        record.headers()
                .add(new RecordHeader(
                        "Idempotency-Key", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(
                        "X-Request-DateTime", Instant.now().toString().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("Accept-Language", "fa".getBytes(StandardCharsets.UTF_8)));

        if (authToken != null && !authToken.isBlank()) {
            record.headers().add(new RecordHeader("Authorization", authToken.getBytes(StandardCharsets.UTF_8)));
        }

        if (operationType != null) {
            record.headers().add(new RecordHeader("operationType", operationType.getBytes(StandardCharsets.UTF_8)));
        }

        if (eventUid != null) {
            record.headers().add(new RecordHeader("eventUid", eventUid.getBytes(StandardCharsets.UTF_8)));
        }

        String traceId = UUID.randomUUID().toString().replace("-", "");
        String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        record.headers()
                .add(new RecordHeader(
                        "traceparent", ("00-" + traceId + "-" + spanId + "-01").getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("tracestate", "e2e=test".getBytes(StandardCharsets.UTF_8)));

        return record;
    }

    public static KafkaConsumer<String, byte[]> createResponseConsumer(
            String bootstrapServers, String topic, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        // SASL_PLAINTEXT with PLAIN mechanism (production-like)
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "PLAIN");
        props.put(
                "sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required "
                        + "username=\"admin\" password=\"admin-secret\";");

        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        return consumer;
    }

    public static ConsumerRecord<String, byte[]> awaitResponse(
            KafkaConsumer<String, byte[]> consumer, String eventUid, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, byte[]> record : records) {
                if (eventUid.equals(record.key())) {
                    return record;
                }
            }
        }
        return null;
    }
}
