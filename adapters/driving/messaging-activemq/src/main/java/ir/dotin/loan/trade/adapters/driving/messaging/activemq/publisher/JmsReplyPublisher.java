package ir.dotin.loan.trade.adapters.driving.messaging.activemq.publisher;

import jakarta.jms.Destination;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.messaging.activemq.dto.JmsCommandReply;

import lombok.RequiredArgsConstructor;

/**
 * Sends request–reply responses over JMS.
 *
 * <p>Resolution order for the reply destination:
 *
 * <ol>
 *   <li>The {@link Destination} from the inbound message's {@code JMSReplyTo} header (typically a temporary queue
 *       created by the caller).
 *   <li>A named queue extracted from the message body's {@code responseTopic} field — compatible with the Kafka
 *       adapter's convention.
 * </ol>
 *
 * <p>If neither is available the reply is silently dropped and a debug log is emitted, which is valid for
 * fire-and-forget commands that happen to flow through this adapter.
 */
@Component
@RequiredArgsConstructor
public class JmsReplyPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(JmsReplyPublisher.class);

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Send a reply, preferring {@code jmsReplyTo} over {@code responseQueueName}.
     *
     * @param jmsReplyTo the JMSReplyTo destination (may be {@code null})
     * @param responseQueueName fallback named queue from the message body (may be {@code null})
     * @param correlationId the JMSCorrelationID to copy onto the reply
     * @param reply the reply payload
     */
    public void sendReply(
            Destination jmsReplyTo, String responseQueueName, String correlationId, JmsCommandReply reply) {

        if (jmsReplyTo != null) {
            sendToDestination(jmsReplyTo, correlationId, reply);
        } else if (responseQueueName != null && !responseQueueName.isBlank()) {
            sendToNamedQueue(responseQueueName, correlationId, reply);
        } else {
            LOG.debug("No reply destination for correlationId={}, dropping reply.", correlationId);
        }
    }

    // ── private helpers ──────────────────────────────────────────────

    private void sendToDestination(Destination destination, String correlationId, JmsCommandReply reply) {
        try {
            String json = objectMapper.writeValueAsString(reply);
            jmsTemplate.send(destination, session -> {
                var message = session.createTextMessage(json);
                if (correlationId != null) {
                    message.setJMSCorrelationID(correlationId);
                }
                message.setStringProperty("status", reply.status().name());
                message.setStringProperty("operationType", reply.operationType());
                return message;
            });
            logSuccess(destination, correlationId);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialise reply [correlationId={}]", correlationId, e);
        } catch (Exception e) {
            LOG.error("Failed to send reply to JMSReplyTo [correlationId={}]", correlationId, e);
        }
    }

    private void sendToNamedQueue(String queueName, String correlationId, JmsCommandReply reply) {
        try {
            String json = objectMapper.writeValueAsString(reply);
            jmsTemplate.send(queueName, session -> {
                var message = session.createTextMessage(json);
                if (correlationId != null) {
                    message.setJMSCorrelationID(correlationId);
                }
                message.setStringProperty("status", reply.status().name());
                message.setStringProperty("operationType", reply.operationType());
                return message;
            });
            LOG.info("Reply published [correlationId={}, queue={}]", correlationId, queueName);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialise reply [correlationId={}]", correlationId, e);
        } catch (Exception e) {
            LOG.error("Failed to send reply to queue={} [correlationId={}]", queueName, correlationId, e);
        }
    }

    private void logSuccess(Destination destination, String correlationId) {
        try {
            LOG.info("Reply published [correlationId={}, destination={}]", correlationId, destination);
        } catch (Exception e) {
            LOG.info("Reply published [correlationId={}]", correlationId);
        }
    }
}
