package ir.dotin.loan.trade.core.application.query.loantype.i18n;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.pangaea.commons.core.error.ErrorCategory;
import ir.dotin.platform.pangaea.commons.core.error.ProductErrorCode;
import ir.dotin.loan.trade.core.domain.shared.error.TradeLoanErrorCategory;

public enum LoanTypeQueryErrorCodes implements ProductErrorCode<LoanTypeQueryErrorCodes> {
    LOAN_TYPE_NOT_FOUND(TradeLoanErrorCategory.TRADE_QUERY, 3, "Loan type with ID {0} not found");

    private final ErrorCategory category;
    private final int sequence;
    private final String defaultMessageFormat;

    LoanTypeQueryErrorCodes(ErrorCategory category, int sequence, String defaultMessageFormat) {
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
