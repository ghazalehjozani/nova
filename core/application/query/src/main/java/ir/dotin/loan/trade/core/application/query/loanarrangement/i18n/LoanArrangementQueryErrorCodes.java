package ir.dotin.loan.trade.core.application.query.loanarrangement.i18n;

import ir.dotin.platform.pangaea.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum LoanArrangementQueryErrorCodes implements LocalizedMessage<LoanArrangementQueryErrorCodes> {
    LOAN_ARRANGEMENT_NOT_FOUND("Loan arrangement with ID {0} not found");

    private final String defaultMessageFormat;
}
