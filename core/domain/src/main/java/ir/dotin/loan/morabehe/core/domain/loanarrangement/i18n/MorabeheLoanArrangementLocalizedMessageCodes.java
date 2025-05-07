package ir.dotin.loan.morabehe.core.domain.loanarrangement.i18n;

import ir.dotin.platform.domain.common.i18n.LocalizedMessage;

public enum MorabeheLoanArrangementLocalizedMessageCodes
        implements LocalizedMessage<MorabeheLoanArrangementLocalizedMessageCodes> {
    INVALID_MORABEHE_POLICY_PARAM("Invalid parameter '{0}' for Morabehe-specific policy. Value: {1}"),
    DUPLICATE_MORABEHE_RULE_CODE("A Morabehe Loan Rule with code '{0}' already exists.");

    private final String messageFormat;

    MorabeheLoanArrangementLocalizedMessageCodes(String defaultMessageFormat) {
        this.messageFormat = defaultMessageFormat;
    }

    @Override
    public String getDefaultMessageFormat() {
        return messageFormat;
    }
}
