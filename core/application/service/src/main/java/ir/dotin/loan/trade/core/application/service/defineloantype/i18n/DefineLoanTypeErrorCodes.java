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
    EMPTY_TOPIC_ASSIGNMENTS("Topic assignments cannot be empty"),
    INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE("Economic sector for loan type {0} does not exist"),
    LOAN_ARRANGEMENT_NOT_FOUND("Loan arrangement with code {0} does not exist");

    private final String defaultMessageFormat;
}
