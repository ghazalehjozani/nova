package ir.dotin.loan.morabehe.core.domain.loanarrangement.i18n;

import ir.dotin.platform.domain.common.i18n.LocalizedMessage;
import ir.dotin.loan.morabehe.core.domain.MorabeheLoanValidationConstants;

public enum MorabeheLoanArrangementLocalizedMessageCodes
        implements LocalizedMessage<MorabeheLoanArrangementLocalizedMessageCodes> {
    INVALID_MORABEHE_POLICY_PARAM(
            "policy.invalid_param", "Invalid parameter '{0}' for Morabehe-specific policy. Value: {1}"),
    DUPLICATE_MORABEHE_RULE_CODE("code.duplicate", "A Morabehe Loan Rule with code '{0}' already exists.");

    private final String constructedKey;
    private final String messageFormat;

    MorabeheLoanArrangementLocalizedMessageCodes(String keySuffix, String defaultMessageFormat) {
        this.constructedKey = MorabeheLoanValidationConstants.LOAN_RULE_KEY_PREFIX + keySuffix;
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
