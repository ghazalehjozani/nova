package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FcbErrorCodeMapper {

    public Notification mapToNotification(FcbBaseResponse response) {
        String errorCode = response.getErrorCode() != null ? response.getErrorCode() : "UNKNOWN";
        String errorMessage = response.getErrorMessage() != null ? response.getErrorMessage() : "No error message";
        return Notification.ofError(CoreBankingErrors.FCB_BUSINESS_ERROR, errorCode, errorMessage);
    }

    public static boolean isServerError(String code) {
        try {
            int httpCode = Integer.parseInt(code);
            return httpCode >= 500 && httpCode <= 599;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isClientError(String code) {
        try {
            int httpCode = Integer.parseInt(code);
            return httpCode >= 400 && httpCode <= 499;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
