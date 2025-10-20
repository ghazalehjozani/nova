package ir.dotin.loan.trade.adapters.driven.fcbclient.i18n;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FcbClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("FCB client error: method={}, status={}, reason={}", methodKey, response.status(), response.reason());

        Notification notification;
        switch (response.status()) { // TODO: fcb always response 200, switch on rs code for business exception
            case 400:
                notification =
                        Notification.ofError(FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, response.reason());
                break;
            case 401:
                notification = Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_AUTHENTICATION_FAILED, response.reason());
                break;
            case 404:
                notification = Notification.ofError(FcbBusinessLocalizedMessageCodes.FCB_ENDPOINT_NOT_FOUND, methodKey);
                break;
            case 500:
            case 503:
                notification = Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_SERVICE_UNAVAILABLE, response.reason());
                break;
            default:
                notification = Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, "Status " + response.status());
        }

        return new FcbBusinessNotification(notification);
    }
}
