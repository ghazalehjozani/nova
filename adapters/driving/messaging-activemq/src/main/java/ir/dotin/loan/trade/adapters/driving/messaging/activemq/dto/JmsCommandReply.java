package ir.dotin.loan.trade.adapters.driving.messaging.activemq.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

/**
 * Wire-format reply sent back to the caller on the JMSReplyTo destination or the body-level {@code responseTopic}
 * queue.
 *
 * <p>Follows the same response contract used by the Kafka adapter ({@code eventUid}, {@code status},
 * {@code errorMessage}) so callers can use a single deserializer regardless of transport.
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JmsCommandReply(
        String correlationId, String operationType, ReplyStatus status, String errorCode, String errorMessage) {

    public enum ReplyStatus {
        SUCCESS,
        FAILED
    }

    public static JmsCommandReply success(String correlationId, String operationType) {
        return JmsCommandReply.builder()
                .correlationId(correlationId)
                .operationType(operationType)
                .status(ReplyStatus.SUCCESS)
                .build();
    }

    public static JmsCommandReply failed(
            String correlationId, String operationType, String errorCode, String errorMessage) {
        return JmsCommandReply.builder()
                .correlationId(correlationId)
                .operationType(operationType)
                .status(ReplyStatus.FAILED)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
