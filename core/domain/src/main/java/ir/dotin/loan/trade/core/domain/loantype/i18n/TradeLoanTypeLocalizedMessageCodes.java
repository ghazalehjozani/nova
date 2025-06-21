package ir.dotin.loan.trade.core.domain.loantype.i18n;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.domain.common.i18n.LocalizedMessage;

public enum TradeLoanTypeLocalizedMessageCodes implements LocalizedMessage<TradeLoanTypeLocalizedMessageCodes> {
    FIELD_REQUIRED("Trade field ''{0}'' is required."),
    HAS_ISSUE_MERCHANDISE_DOC_NULL("'hasIssueMerchandiseDocument' cannot be null."),
    TRADE_LOAN_RULES_EMPTY("Trade Loan Rule IDs cannot be empty."),
    TRADE_LOAN_RULES_CONTAIN_NULL("Trade Loan Rule IDs cannot contain null elements."),
    BUILDER_VALIDATION_FAILED("Failed to build MorabeheLoanType due to validation errors."),
    CANNOT_CREATE_NEW_VERSION_FROM_DISABLED("Cannot create a new version from a DISABLED Loan Type version (ID: {0})."),
    CANNOT_CREATE_NEW_VERSION_FROM_INACTIVE(
            "Cannot create a new version from an INACTIVE Loan Type version (ID: {0}).");

    private final String messageFormat;

    TradeLoanTypeLocalizedMessageCodes(String defaultMessageFormat) {
        this.messageFormat = defaultMessageFormat;
    }

    @Override
    @NonNull
    public String getDefaultMessageFormat() {
        return messageFormat;
    }
}
