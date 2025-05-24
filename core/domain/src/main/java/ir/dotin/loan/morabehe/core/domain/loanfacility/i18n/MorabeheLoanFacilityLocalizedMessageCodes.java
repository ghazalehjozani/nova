package ir.dotin.loan.morabehe.core.domain.loanfacility.i18n;

import ir.dotin.platform.domain.common.i18n.LocalizedMessage;

public enum MorabeheLoanFacilityLocalizedMessageCodes
        implements LocalizedMessage<MorabeheLoanFacilityLocalizedMessageCodes> {
    BUILDER_VALIDATION_FAILED("Failed to build MorabeheLoanApplication due to validation errors."),
    LOAN_TYPE_ID_REQUIRED("Morabehe Loan Type ID is required."),
    LOAN_RULE_ID_REQUIRED("Morabehe Loan Rule ID is required."),
    ARRANGEMENT_FETCH_FAILED("Failed to fetch Loan Arrangement details for ID: {0}"),
    INTEREST_CALCULATION_FAILED("Failed to calculate interest: {0}"),
    TRANSACTION_POSTING_FAILED("Failed to post transaction."),
    UNKNOWN_STRATEGY_TYPE("Cannot determine purpose code for unknown strategy type: {0}"),
    LOAN_TOPIC_RESOLUTION_FAILED("Failed to resolve primary loan topic for LoanType ID {0} and EconomicSector {1}"),
    CANNOT_POST_DISBURSE_TRANSACTION("Cannot post disbursement transactions while facility is in state: {0}"),
    UNEXPECTED_ERROR("An unexpected error occurred: {0}"),

    INTEREST_FORMULA_MISSING("Interest calculation failed: Formula is missing in the InterestPolicy."),
    INTEREST_FORMULA_EVALUATION_FAILED("Interest calculation failed during formula evaluation"),
    INTEREST_RESULT_CONVERSION_FAILED("Interest calculation failed: Could not convert result to Money: {0}"),
    INTEREST_CALCULATION_UNEXPECTED_ERROR("Unexpected error during interest calculation: {0}"),

    CALCULATION_CONTEXT_LOAN_APPLICATION_MISSING_IN_FACILITY(
            "LoanApplication is missing within the provided facility."),
    CALCULATION_CONTEXT_ECONOMIC_SECTOR_MISSING("EconomicSector is missing from the LoanApplication."),
    CALCULATION_CONTEXT_APPLICATION_NUMBER_MISSING(
            "ApplicationNumber is missing from the LoanApplication, cannot create PostTitle."),
    CALCULATION_CONTEXT_POST_TITLE_CREATION_FAILED("Failed to create PostTitle from application number: {0}.");

    private final String messageFormat;

    MorabeheLoanFacilityLocalizedMessageCodes(String defaultMessageFormat) {
        this.messageFormat = defaultMessageFormat;
    }

    @Override
    public String getDefaultMessageFormat() {
        return messageFormat;
    }
}
