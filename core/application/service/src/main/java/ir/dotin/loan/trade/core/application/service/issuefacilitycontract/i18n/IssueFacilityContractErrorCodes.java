package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum IssueFacilityContractErrorCodes implements LocalizedMessage<IssueFacilityContractErrorCodes> {
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    LOAN_TYPE_NOT_FOUND("Loan type {0} not found for facility: {1}"),
    LOAN_ARRANGEMENT_NOT_FOUND("Loan arrangement with ID {0} not found"),
    BRANCH_CODE_NOT_FOUND("Branch code not found in token"),
    FACILITY_NOT_APPROVED("Facility {0} has not been approved"),
    INVALID_STATE("Facility {0} is in invalid state for contract issuance"),
    MISSING_TRANSACTION_NUMBERS("Transaction numbers are required"),
    INVALID_POST_TITLE("Invalid post title: {0}"),
    SANCTIONED_LOAN_NOT_FOUND("Sanctioned loan not found for facility {0}"),
    APPROVED_AMOUNT_NOT_FOUND("Approved amount not found for sanctioned loan"),
    INVALID_ARTICLE_COMPONENT("Invalid article component"),
    CURRENCY_NOT_FOUND("Currency not found for sanctioned loan"),
    BRANCH_NOT_FOUND("Branch not found for facility"),
    TRANSACTION_CALCULATION_FAILED("Failed to calculate transaction for facility {0}"),
    TRANSACTION_POSTING_FAILED("Failed to post transaction for facility {0}");

    private final String defaultMessageFormat;
}
