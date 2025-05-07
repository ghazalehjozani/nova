package ir.dotin.loan.morabehe.core.domain.loantype.i18n;

import ir.dotin.platform.domain.common.i18n.LocalizedMessage;

public enum MorabeheLoanTypeLocalizedMessageCodes implements LocalizedMessage<MorabeheLoanTypeLocalizedMessageCodes> {
    FIELD_REQUIRED("Morabehe field ''{0}'' is required."),
    HAS_ISSUE_MERCHANDISE_DOC_NULL("'hasIssueMerchandiseDocument' cannot be null."),
    MORABEHE_LOAN_RULES_EMPTY("Morabehe Loan Rule IDs cannot be empty."),
    MORABEHE_LOAN_RULES_CONTAIN_NULL("Morabehe Loan Rule IDs cannot contain null elements."),
    BUILDER_VALIDATION_FAILED("Failed to build MorabeheLoanType due to validation errors."),
    CANNOT_CREATE_NEW_VERSION_FROM_DISABLED("Cannot create a new version from a DISABLED Loan Type version (ID: {0})."),
    CANNOT_CREATE_NEW_VERSION_FROM_INACTIVE(
            "Cannot create a new version from an INACTIVE Loan Type version (ID: {0}).");

    private final String messageFormat;

    MorabeheLoanTypeLocalizedMessageCodes(String defaultMessageFormat) {
        this.messageFormat = defaultMessageFormat;
    }

    @Override
    public String getDefaultMessageFormat() {
        return messageFormat;
    }
}
