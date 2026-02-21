package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbKafkaLocalizedMessageCodes;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaErrorCodeMapper {

    public Notification mapToNotification(FcbKafkaBaseResponse response) {
        String errorCode = response.getErrorCode() != null ? response.getErrorCode() : "UNKNOWN";
        String errorMessage = response.getErrorMessage() != null ? response.getErrorMessage() : "No error message";
        return Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_FCB_BUSINESS_ERROR, errorCode, errorMessage);
    }
}
