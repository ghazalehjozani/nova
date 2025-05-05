package ir.dotin.loan.morabehe.core.domain.loantype.i18n;

import ir.dotin.platform.domain.common.i18n.LocalizedMessage;
import ir.dotin.loan.morabehe.core.domain.MorabeheLoanValidationConstants;

public enum MorabeheLoanTypeLocalizedMessageCodes implements LocalizedMessage<MorabeheLoanTypeLocalizedMessageCodes> {
    FIELD_REQUIRED("field_required", "Morabehe field ''{0}'' is required."),
    HAS_ISSUE_MERCHANDISE_DOC_NULL("merch_doc_flag_null", "'hasIssueMerchandiseDocument' cannot be null."),
    MORABEHE_LOAN_RULES_EMPTY("morabehe_rules_empty", "Morabehe Loan Rule IDs cannot be empty."),
    MORABEHE_LOAN_RULES_CONTAIN_NULL("morabehe_rules_null", "Morabehe Loan Rule IDs cannot contain null elements."),
    BUILDER_VALIDATION_FAILED(
            "builder.validation_failed", "Failed to build MorabeheLoanType due to validation errors."),
    CANNOT_CREATE_NEW_VERSION_FROM_DISABLED(
            "versioning.cannot_supersede_disabled",
            "Cannot create a new version from a DISABLED Loan Type version (ID: {0})."),
    CANNOT_CREATE_NEW_VERSION_FROM_INACTIVE(
            "versioning.cannot_supersede_inactive",
            "Cannot create a new version from an INACTIVE Loan Type version (ID: {0}).");

    private final String constructedKey;
    private final String messageFormat;

    MorabeheLoanTypeLocalizedMessageCodes(String keySuffix, String defaultMessageFormat) {
        this.constructedKey = MorabeheLoanValidationConstants.LOAN_TYPE_KEY_PREFIX + keySuffix;
        this.messageFormat = defaultMessageFormat;
    }

    @Override
    public String getKey() {
        return constructedKey;
    }

    @Override
    public String getDefaultMessageFormat() {
        return messageFormat;
    }
}
