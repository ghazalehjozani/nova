package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbJmsLocalizedMessageCodes;

import lombok.experimental.UtilityClass;

/** Maps FCB JMS error responses to {@link Notification} instances. */
@UtilityClass
public class JmsErrorCodeMapper {

    public Notification mapToNotification(FcbJmsResponse response) {
        String errorCode = response.errorCode() != null ? response.errorCode() : "UNKNOWN";
        String errorMessage = response.errorMessage() != null ? response.errorMessage() : "No error message";

        return Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_FCB_BUSINESS_ERROR, errorCode, errorMessage);
    }
}
