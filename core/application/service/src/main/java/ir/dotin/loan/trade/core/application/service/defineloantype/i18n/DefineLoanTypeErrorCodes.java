package ir.dotin.loan.trade.core.application.service.defineloantype.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum DefineLoanTypeErrorCodes implements LocalizedMessage<DefineLoanTypeErrorCodes> {
    LOAN_TYPE_ALREADY_EXISTS("Loan type with Code {0} already exists"),
    DUPLICATE_CODE("Loan type with code {0} already exists"),
    INVALID_ARRANGEMENT_IDS("Invalid loan arrangement IDs provided"),
    INVALID_INCOME_IDS("Invalid income IDs provided"),
    EMPTY_TOPIC_ASSIGNMENTS("Topic assignments cannot be empty");

    private final String defaultMessageFormat;
}
