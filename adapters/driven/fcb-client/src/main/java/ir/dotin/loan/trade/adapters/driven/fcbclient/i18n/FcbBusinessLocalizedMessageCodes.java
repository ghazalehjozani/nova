package ir.dotin.loan.trade.adapters.driven.fcbclient.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum FcbBusinessLocalizedMessageCodes implements LocalizedMessage<FcbBusinessLocalizedMessageCodes> {
    FCB_BAD_REQUEST("Bad request to FCB service: {0}"),
    FCB_AUTHENTICATION_FAILED("FCB authentication failed: {0}"),
    FCB_ENDPOINT_NOT_FOUND("FCB endpoint not found: {0}"),
    FCB_SERVICE_UNAVAILABLE("FCB service is unavailable: {0}"),
    FCB_UNKNOWN_ERROR("Unknown FCB error: {0}"),
    FCB_BUSINESS_EXCEPTION("FCB business exception: {0}"),
    FCB_TRANSACTION_FAILED("FCB transaction failed: {0}"),
    FCB_INVALID_RESPONSE("Invalid response from FCB: {0}"),
    CUSTOMER_NOT_FOUND_IN_FCB("Customer with national code {0} not found in FCB"),
    ACCOUNT_NUMBER_NOT_FOUND("Account number {0} not found");

    private final String defaultMessageFormat;
}
