package ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum FcbKafkaLocalizedMessageCodes implements LocalizedMessage<FcbKafkaLocalizedMessageCodes> {
    KAFKA_REPLY_TIMEOUT("Kafka reply timed out for operation {0} after {1}ms"),
    KAFKA_COMMUNICATION_ERROR("Kafka communication error: {0}"),
    KAFKA_BROKER_UNAVAILABLE("Kafka broker is unavailable: {0}"),
    KAFKA_INVALID_RESPONSE("Invalid Kafka response payload for operation {0}"),
    KAFKA_FCB_BUSINESS_ERROR("FCB business error via Kafka: code={0}, message={1}"),
    KAFKA_FCB_SERVER_ERROR("FCB server error via Kafka (5xx): code={0}, message={1}"),
    KAFKA_FCB_CLIENT_ERROR("FCB client error via Kafka (4xx): code={0}, message={1}"),
    KAFKA_SERIALIZATION_ERROR("Failed to serialize/deserialize Kafka message: {0}");

    private final String defaultMessageFormat;
}
