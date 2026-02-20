package ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum FcbJmsLocalizedMessageCodes implements LocalizedMessage<FcbJmsLocalizedMessageCodes> {
    JMS_REPLY_TIMEOUT("JMS reply timed out for operation {0} after {1}ms"),
    JMS_COMMUNICATION_ERROR("JMS communication error: {0}"),
    JMS_BROKER_UNAVAILABLE("ActiveMQ broker is unavailable: {0}"),
    JMS_INVALID_RESPONSE("Invalid JMS response payload for operation {0}"),
    JMS_FCB_BUSINESS_ERROR("FCB business error via JMS: code={0}, message={1}");

    private final String defaultMessageFormat;
}
