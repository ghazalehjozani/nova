package ir.dotin.loan.trade.core.domain.loanarrangement.i18n;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.domain.common.i18n.LocalizedMessage;

public enum TradeLoanArrangementLocalizedMessageCodes
        implements LocalizedMessage<TradeLoanArrangementLocalizedMessageCodes> {
    INVALID_TRADE_POLICY_PARAM("Invalid parameter '{0}' for Trade-specific policy. Value: {1}"),
    DUPLICATE_TRADE_RULE_CODE("A Trade Loan Rule with code '{0}' already exists.");

    private final String messageFormat;

    TradeLoanArrangementLocalizedMessageCodes(String defaultMessageFormat) {
        this.messageFormat = defaultMessageFormat;
    }

    @Override
    @NonNull
    public String getDefaultMessageFormat() {
        return messageFormat;
    }
}
