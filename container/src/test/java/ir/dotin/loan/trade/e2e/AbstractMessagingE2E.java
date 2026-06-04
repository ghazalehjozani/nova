package ir.dotin.loan.trade.e2e;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

public abstract class AbstractMessagingE2E extends AbstractE2E {

    @Autowired
    protected KafkaTemplate<String, byte[]> kafkaTemplate;

    protected ProducerRecord<String, byte[]> buildRecord(String topic, String key, Object payload) throws Exception {
        byte[] value = objectMapper.writeValueAsBytes(payload);
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, value);

        String eventUid = UUID.randomUUID().toString();
        record.headers()
                .add(new RecordHeader("eventUid", eventUid.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("occurredAt", Instant.now().toString().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("Accept-Language", "fa".getBytes(StandardCharsets.UTF_8)));

        if (authToken != null && !authToken.isBlank()) {
            record.headers().add(new RecordHeader("Authorization", authToken.getBytes(StandardCharsets.UTF_8)));
        }

        record.headers()
                .add(new RecordHeader(
                        "traceparent",
                        ("00-" + UUID.randomUUID().toString().replace("-", "")
                                        + "-"
                                        + UUID.randomUUID()
                                                .toString()
                                                .replace("-", "")
                                                .substring(0, 16)
                                        + "-01")
                                .getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("tracestate", "e2e=test".getBytes(StandardCharsets.UTF_8)));

        return record;
    }

    protected void sendAndWait(ProducerRecord<String, byte[]> record)
            throws ExecutionException, InterruptedException, TimeoutException {
        kafkaTemplate.send(record).get(30, TimeUnit.SECONDS);
    }
}
