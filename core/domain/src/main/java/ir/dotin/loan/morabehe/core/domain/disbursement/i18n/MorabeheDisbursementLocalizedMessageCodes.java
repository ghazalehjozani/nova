package ir.dotin.loan.morabehe.core.domain.disbursement.i18n;

import ir.dotin.platform.domain.common.i18n.LocalizedMessage;

public enum MorabeheDisbursementLocalizedMessageCodes
        implements LocalizedMessage<MorabeheDisbursementLocalizedMessageCodes> {
    INVALID_STATE_FOR_COMPLETION("Cannot complete Morabehe disbursement (ID: {0}) in state: {1}"),
    INVALID_STATE_FOR_PENDING("Cannot mark pending Morabehe disbursement (ID: {0}) in state: {1}"),
    INVALID_STATE_FOR_FAILURE("Cannot mark failed Morabehe disbursement (ID: {0}) in state: {1}"),
    TRANSACTION_NUMBERS_NULL_ON_COMPLETION(
            "Transaction Numbers cannot be null or empty for completed Morabehe disbursement (ID: {0})."),
    FAILURE_REASON_NULL("Failure reason cannot be null for failed Morabehe disbursement (ID: {0})."),
    FIELD_REQUIRED("Field ''{0}'' is required for disbursement."),
    COLLECTION_NULL_ELEMENTS("Collection ''{0}'' cannot contain null elements."),
    CALCULATION_CONTEXT_LOAN_APPLICATION_MISSING_IN_FACILITY(
            "LoanApplication is missing within the provided facility."),
    CALCULATION_CONTEXT_ECONOMIC_SECTOR_MISSING("EconomicSector is missing from the LoanApplication."),
    CALCULATION_CONTEXT_APPLICATION_NUMBER_MISSING(
            "ApplicationNumber is missing from the LoanApplication, cannot create PostTitle."),
    CALCULATION_CONTEXT_POST_TITLE_CREATION_FAILED("Failed to create PostTitle from application number: {0}."),
    UNKNOWN_STRATEGY_TYPE("Cannot determine purpose code for unknown strategy type: {0}"),
    LOAN_TOPIC_RESOLUTION_FAILED("Failed to resolve primary loan topic for LoanType ID {0} and EconomicSector {1}"),
    CANNOT_POST_DISBURSE_TRANSACTION("Cannot post disbursement transactions while facility is in state: {0}"),
    UNEXPECTED_ERROR("An unexpected error occurred: {0}"),
    ARRANGEMENT_FETCH_FAILED("Failed to fetch Loan Arrangement details for ID: {0}"),
    INTEREST_FORMULA_MISSING("Interest calculation failed: Formula is missing in the InterestPolicy."),
    INTEREST_FORMULA_EVALUATION_FAILED("Interest calculation failed during formula evaluation"),
    INTEREST_RESULT_CONVERSION_FAILED("Interest calculation failed: Could not convert result to Money: {0}"),
    INTEREST_CALCULATION_UNEXPECTED_ERROR("Unexpected error during interest calculation: {0}"),
    TRANSACTION_METADATA_DEFAULT_CREATION_FAILED("Default transaction metadata creation failed."),
    DEPOSIT_NUMBER_REQUIRED_FOR_DEPOSIT_TYPE("Deposit number is required for deposit type destination.");

    private final String messageFormat;

    MorabeheDisbursementLocalizedMessageCodes(String defaultMessageFormat) {
        this.messageFormat = defaultMessageFormat;
    }

    @Override
    public String getDefaultMessageFormat() {
        return messageFormat;
    }
}
