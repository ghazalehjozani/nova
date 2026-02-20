package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

import org.springframework.context.annotation.Profile;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbMessagingProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbJmsLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.JmsErrorCodeMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * Centralized JMS client for sending request-reply messages to FCB via ActiveMQ. Blocks the calling virtual thread
 * until a reply is received or the timeout expires.
 */
@Slf4j
@Component
@Profile("activemq")
@RequiredArgsConstructor
public class FcbJmsClient {

    private final ObjectMapper objectMapper;

    /**
     * Sends a JMS request and waits for a correlated reply.
     *
     * @param jmsTemplate the queue-specific JmsTemplate
     * @param request the FCB JMS request
     * @param queueConfig the queue configuration (for reply queue and timeout)
     * @return Result containing the response, or a failure notification on error/timeout
     */
    public Result<FcbJmsResponse> sendAndReceive(
            JmsTemplate jmsTemplate, FcbJmsRequest request, FcbMessagingProperties.QueueConfig queueConfig) {

        log.debug(
                "Sending JMS request: operation={}, correlationId={}",
                request.operationName(),
                request.correlationId());

        try {
            String requestJson = objectMapper.writeValueAsString(request);

            Message replyMessage = jmsTemplate.sendAndReceive(queueConfig.requestQueue(), session -> {
                TextMessage message = session.createTextMessage(requestJson);
                message.setJMSCorrelationID(request.correlationId());
                message.setStringProperty("operationName", request.operationName());
                return message;
            });

            if (replyMessage == null) {
                log.warn(
                        "JMS reply timed out: operation={}, correlationId={}, timeout={}ms",
                        request.operationName(),
                        request.correlationId(),
                        queueConfig.receiveTimeoutMs());
                return Result.failure(Notification.ofError(
                        FcbJmsLocalizedMessageCodes.JMS_REPLY_TIMEOUT,
                        request.operationName(),
                        String.valueOf(queueConfig.receiveTimeoutMs())));
            }

            FcbJmsResponse response = deserializeResponse(replyMessage);

            if (response.isError()) {
                log.warn(
                        "JMS FCB error response: operation={}, errorCode={}, errorMessage={}",
                        request.operationName(),
                        response.errorCode(),
                        response.errorMessage());
                return Result.failure(JmsErrorCodeMapper.mapToNotification(response));
            }

            log.debug(
                    "JMS reply received: operation={}, correlationId={}",
                    request.operationName(),
                    response.correlationId());
            return Result.success(response);

        } catch (JmsException e) {
            log.error("JMS communication error: operation={}", request.operationName(), e);
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_COMMUNICATION_ERROR, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during JMS request: operation={}", request.operationName(), e);
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_COMMUNICATION_ERROR, e.getMessage()));
        }
    }

    private FcbJmsResponse deserializeResponse(Message message) throws JMSException {
        try {
            if (message instanceof TextMessage textMessage) {
                String json = textMessage.getText();
                return objectMapper.readValue(json, FcbJmsResponse.class);
            }
            throw new JMSException(
                    "Unexpected message type: " + message.getClass().getSimpleName());
        } catch (JMSException e) {
            throw e;
        } catch (Exception e) {
            throw new JMSException("Failed to deserialize JMS response: " + e.getMessage());
        }
    }
}
