package ir.dotin.loan.trade.adapters.driven.fcbclient.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum FcbBusinessLocalizedMessageCodes implements LocalizedMessage<FcbBusinessLocalizedMessageCodes> {
    FCB_BAD_REQUEST("Bad request to FCB service: {0}"), // TODO: REMOVE
    FCB_AUTHENTICATION_FAILED("FCB authentication failed: {0}"), // TODO: REMOVE
    FCB_ENDPOINT_NOT_FOUND("FCB endpoint not found: {0}"), // TODO: REMOVE
    FCB_SERVICE_UNAVAILABLE("FCB service is unavailable: {0}"), // TODO: REMOVE
    FCB_UNKNOWN_ERROR("Unknown FCB error: {0}"), // TODO: REMOVE
    FCB_BUSINESS_EXCEPTION("FCB business exception: {0}"), // TODO: REMOVE
    FCB_TRANSACTION_FAILED("FCB transaction failed: {0}"), // TODO: REMOVE
    FCB_INVALID_RESPONSE("Invalid response from FCB: {0}"), // TODO: REMOVE
    CUSTOMER_NOT_FOUND_IN_FCB("Customer with national code {0} not found in FCB"),
    ACCOUNT_NUMBER_NOT_FOUND("Account number {0} not found"),
    FCB_MULTIPLE_TRANSACTION_CODES("Multiple transaction codes returned from FCB"),
    FCB_MISSING_TRANSACTION_CODE("Missing transaction code"),
    UNKNOWN_ERROR("unknown error"),
    INVALID_DEPOSIT_NUMBER("Invalid deposit number: {0}"),
    INVALID_CUSTOMER_NUMBER("Invalid customer number: {0}"),
    INVALID_REQUEST_REASON("Invalid request reason: {0}"),
    INVALID_SUB_SOURCE_CODE("Invalid sub source code: {0}"),
    INVALID_ECONOMICAL_SECTOR_CODE("Invalid economical sector code: {0}"),
    UNSUPPORTED_ECONOMICAL_SECTOR_FOR_LOAN_TYPE("Loan type {0} does not support economical sector {1}"),
    UNSUPPORTED_CURRENCY_FOR_DEPOSIT("Deposit {0} does not support currency {1}"),
    INVALID_SERVICE_INPUT_FOR_PARAMETER_SERVICE_OPEN_ACCOUNT("Invalid Service nova-open-account Input for parameter {0}");

    private final String defaultMessageFormat;
}
