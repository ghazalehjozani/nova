package ir.dotin.loan.trade.adapters.driving.messaging.activemq.support;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Optional;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ir.dotin.platform.adapter.messaging.command.model.RawCommandMessage;

// TODO: Remove after update platform messaging structure
public final class JmsRawCommandMessageFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JmsRawCommandMessageFactory.class);

    private static final String SYNTHETIC_TOPIC = "jms-bridge";

    private JmsRawCommandMessageFactory() {}

    public static RawCommandMessage from(Message jmsMessage, String topic) throws JMSException {
        String body = extractBody(jmsMessage);
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);

        String key = jmsMessage.getJMSCorrelationID();

        RecordHeaders headers = mapJmsPropertiesToHeaders(jmsMessage);

        // Synthetic ConsumerRecord so the platform pipeline accepts JMS payloads
        ConsumerRecord<String, byte[]> syntheticRecord = new ConsumerRecord<>(
                topic != null ? topic : SYNTHETIC_TOPIC,
                0,
                0L,
                System.currentTimeMillis(),
                TimestampType.NO_TIMESTAMP_TYPE,
                0,
                payload.length,
                key,
                payload,
                headers,
                Optional.empty());

        return RawCommandMessage.from(syntheticRecord);
    }

    // ── internals ────────────────────────────────────────────────────

    private static String extractBody(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            return textMessage.getText();
        }
        throw new IllegalArgumentException("Unsupported JMS message type: "
                + message.getClass().getName() + ". Only TextMessage with JSON payload is supported.");
    }

    @SuppressWarnings("unchecked")
    private static RecordHeaders mapJmsPropertiesToHeaders(Message message) throws JMSException {
        var headers = new RecordHeaders();

        Enumeration<?> names = message.getPropertyNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = message.getStringProperty(name);
            if (value != null) {
                headers.add(new RecordHeader(name, value.getBytes(StandardCharsets.UTF_8)));
            }
        }

        String correlationId = message.getJMSCorrelationID();
        if (correlationId != null) {
            headers.add(new RecordHeader("JMSCorrelationID", correlationId.getBytes(StandardCharsets.UTF_8)));
        }

        String messageId = message.getJMSMessageID();
        if (messageId != null) {
            headers.add(new RecordHeader("JMSMessageID", messageId.getBytes(StandardCharsets.UTF_8)));
        }

        return headers;
    }
}
