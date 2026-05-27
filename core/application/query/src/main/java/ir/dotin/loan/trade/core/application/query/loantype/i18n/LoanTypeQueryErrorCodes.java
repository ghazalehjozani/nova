package ir.dotin.loan.trade.core.application.query.loantype.i18n;

import ir.dotin.platform.pangaea.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum LoanTypeQueryErrorCodes implements LocalizedMessage<LoanTypeQueryErrorCodes> {
    LOAN_TYPE_NOT_FOUND("Loan type with ID {0} not found");

    private final String defaultMessageFormat;
}
