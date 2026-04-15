package ir.dotin.loan.trade.core.application.ports.outbound.client.error;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.error.ErrorCategory;
import ir.dotin.platform.commons.core.error.PlatformErrorCategory;
import ir.dotin.platform.commons.core.error.ProductErrorCode;
import ir.dotin.loan.trade.core.domain.shared.error.TradeLoanErrorCategory;

/*
 * Sequences per category for this file:
 *   CORE_BANKING_INTEGRATION (51): 1–30,  reserved 31–50
 *   INTEGRATION              (03): 1–10,  reserved —
 *
 * Note: 8 deprecated FCB constants (TODO:REMOVE) from original
 * FcbBusinessLocalizedMessageCodes have been removed.
 */
public enum CoreBankingErrors implements ProductErrorCode<CoreBankingErrors> {

    // ── CORE_BANKING_INTEGRATION (51) — FCB business-level errors ───────────────

    CUSTOMER_NOT_FOUND_IN_FCB(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 1, "Customer with national code {0} not found in FCB"),
    ACCOUNT_NUMBER_NOT_FOUND(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 2, "Account number {0} not found"),
    FCB_MULTIPLE_TRANSACTION_CODES(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 3, "Multiple transaction codes returned from FCB"),
    FCB_MISSING_TRANSACTION_CODE(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 4, "Missing transaction code"),
    UNKNOWN_ERROR(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 5, "Unknown error from core banking system: {0}"),
    INVALID_DEPOSIT_NUMBER(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 6, "Invalid deposit number: {0}"),
    INVALID_CUSTOMER_NUMBER(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 7, "Invalid customer number: {0}"),
    INVALID_REQUEST_REASON(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 8, "Invalid request reason: {0}"),
    INVALID_SUB_SOURCE_CODE(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 9, "Invalid sub source code: {0}"),
    INVALID_ECONOMICAL_SECTOR_CODE(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 10, "Invalid economical sector code: {0}"),
    UNSUPPORTED_ECONOMICAL_SECTOR_FOR_LOAN_TYPE(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION,
            11,
            "Loan type {0} does not support economical sector {1}"),
    UNSUPPORTED_CURRENCY_FOR_DEPOSIT(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 12, "Deposit {0} does not support currency {1}"),
    INVALID_SERVICE_INPUT_FOR_PARAMETER_SERVICE_OPEN_ACCOUNT(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION,
            13,
            "Invalid Service nova-open-account Input for parameter {0}"),
    FCB_RESPONSE_CAN_NOT_MAP(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 14, "Could not map fcb response"),
    INVALID_ROLLBACK_ID(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 15, "Invalid rollback id: {0}"),
    ROLLBACK_ID_AND_ACCOUNT_NUMBER_BOTH_PRESENT(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION,
            16,
            "Rollback ID and account number cannot be provided simultaneously"),
    INVALID_ACCOUNT_NUMBER(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 17, "Invalid account number: {0}"),
    INVALID_FILE_NUMBER(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 18, "Invalid file number: {0}"),
    INVALID_SERVICE_INPUT_FOR_UN_RESERVE(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION,
            19,
            "Either 'rollbackId' OR both 'fileNumber' and 'collateralSerial' must be provided"),
    BRANCH_NOT_COVERED(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 24, "Branch {0} is not covered"),

    // FCB Kafka business-level errors,
    KAFKA_INVALID_RESPONSE(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 20, "Invalid Kafka response payload for operation {0}"),
    KAFKA_FCB_BUSINESS_ERROR(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 21, "FCB business error: code={0}, message={1}"),
    KAFKA_FCB_SERVER_ERROR(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION,
            22,
            "FCB server error via Kafka (5xx): code={0}, message={1}"),
    KAFKA_FCB_CLIENT_ERROR(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION,
            23,
            "FCB client error via Kafka (4xx): code={0}, message={1}"),
    SAMAT_INVALID_USE_TYPE(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 25, "نوع مصرف با کد {0} معتبر نمی‌باشد"),
    SAMAT_INVALID_ISIC_ECONOMIC_SECTOR(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION,
            26,
            "کد بخش اقتصادی ISIC با مقدار {0} برای بخش اقتصادی انتخاب‌شده تعریف نشده است"),
    SAMAT_INVALID_ISIC_SUB_COMBINATION(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION,
            27,
            "زیربخش ISIC با مقدار {0} با بخش اقتصادی {1} همخوانی ندارد"),
    SAMAT_INVALID_EXCEPTION_CODE(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 28, "کد استثناء با مقدار {0} در سیستم تعریف نشده است"),
    SAMAT_INVALID_CONSUMPTION_PLACE_CODE(
            TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 29, "کد محل مصرف با مقدار {0} معتبر نمی‌باشد"),
    SAMAT_UNKNOWN_VIOLATION(TradeLoanErrorCategory.CORE_BANKING_INTEGRATION, 30, "خطای ناشناخته سامات با کد {0}: {1}"),

    // ── INTEGRATION (03) — transport-level errors ───────────────────────────────

    KAFKA_REPLY_TIMEOUT(PlatformErrorCategory.INTEGRATION, 1, "Kafka reply timed out for operation {0} after {1}ms"),
    KAFKA_COMMUNICATION_ERROR(PlatformErrorCategory.INTEGRATION, 2, "Kafka communication error: {0}"),
    KAFKA_BROKER_UNAVAILABLE(PlatformErrorCategory.INTEGRATION, 3, "Kafka broker is unavailable: {0}"),
    KAFKA_SERIALIZATION_ERROR(
            PlatformErrorCategory.INTEGRATION, 4, "Failed to serialize/deserialize Kafka message: {0}");

    private final ErrorCategory category;
    private final int sequence;
    private final String defaultMessageFormat;

    CoreBankingErrors(ErrorCategory category, int sequence, String defaultMessageFormat) {
        this.category = category;
        this.sequence = sequence;
        this.defaultMessageFormat = defaultMessageFormat;
        category.validate();
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

    @Override
    public int sequence() {
        return sequence;
    }

    @Override
    @NonNull
    public String getDefaultMessageFormat() {
        return defaultMessageFormat;
    }
}
