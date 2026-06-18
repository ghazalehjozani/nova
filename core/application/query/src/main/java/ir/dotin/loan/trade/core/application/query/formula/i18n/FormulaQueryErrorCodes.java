package ir.dotin.loan.trade.core.application.query.formula.i18n;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.pangaea.commons.core.error.ErrorCategory;
import ir.dotin.platform.pangaea.commons.core.error.ProductErrorCode;
import ir.dotin.loan.trade.core.domain.shared.error.TradeLoanErrorCategory;

public enum FormulaQueryErrorCodes implements ProductErrorCode<FormulaQueryErrorCodes> {
    FORMULA_NOT_FOUND(TradeLoanErrorCategory.TRADE_QUERY, 5, "Formula with code {0} not found"),
    PROVIDER_NOT_FOUND(TradeLoanErrorCategory.TRADE_QUERY, 6, "Formula binding provider {0} not found");

    private final ErrorCategory category;
    private final int sequence;
    private final String defaultMessageFormat;

    FormulaQueryErrorCodes(ErrorCategory category, int sequence, String defaultMessageFormat) {
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
