package ir.dotin.loan.trade.adapters.driven.fcbclient.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FcbBusinessLocalizedMessageCodes implements LocalizedMessage<FcbBusinessLocalizedMessageCodes> {
    FCB_BAD_REQUEST("FCB_BAD_REQUEST"),
    FCB_AUTHENTICATION_FAILED("FCB_AUTHENTICATION_FAILED"),
    FCB_ENDPOINT_NOT_FOUND("FCB_ENDPOINT_NOT_FOUND"),
    FCB_SERVICE_UNAVAILABLE("FCB_SERVICE_UNAVAILABLE"),
    FCB_UNKNOWN_ERROR("FCB_UNKNOWN_ERROR"),
    FCB_BUSINESS_EXCEPTION("FCB_BUSINESS_EXCEPTION"),
    FCB_TRANSACTION_FAILED("FCB_TRANSACTION_FAILED"),
    FCB_INVALID_RESPONSE("FCB_INVALID_RESPONSE"),
    CUSTOMER_NOT_FOUND_IN_FCB("CUSTOMER_NOT_FOUND_IN_FCB"),
    ACCOUNT_NUMBER_NOT_FOUND("ACCOUNT_NUMBER_NOT_FOUND"),
    ;

    private final String defaultMessageFormat;
}
