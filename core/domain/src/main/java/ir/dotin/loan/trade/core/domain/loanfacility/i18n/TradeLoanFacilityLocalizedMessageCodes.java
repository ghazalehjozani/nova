package ir.dotin.loan.trade.core.domain.loanfacility.i18n;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

public enum TradeLoanFacilityLocalizedMessageCodes implements LocalizedMessage<TradeLoanFacilityLocalizedMessageCodes> {
    BUILDER_VALIDATION_FAILED("Failed to build MorabeheLoanApplication due to validation errors."),
    LOAN_TYPE_ID_REQUIRED("Trade Loan Type ID is required."),
    LOAN_RULE_ID_REQUIRED("Trade Loan Rule ID is required."),
    ARRANGEMENT_FETCH_FAILED("Failed to fetch Loan Arrangement details for ID: {0}"),
    INTEREST_CALCULATION_FAILED("Failed to calculate interest: {0}"),
    TRANSACTION_POSTING_FAILED("Failed to post transaction."),
    ALL_DISBURSEMENT_INPUTS_REQUIRED("All disbursement inputs are required"),
    FACILITY_CANNOT_BE_NULL("Facility cannot be null"),
    LOAN_TYPE_CANNOT_BE_NULL("Loan type cannot be null"),
    BRANCH_CODE_CANNOT_BE_NULL("Branch code cannot be null"),
    POST_TITLE_CANNOT_BE_NULL("Post title cannot be null"),
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
    CALCULATION_CONTEXT_POST_TITLE_CREATION_FAILED("Failed to create PostTitle from application number: {0}."),
    STRATEGY_RETURNED_NO_ITEMS("Strategy did not return any items when items were expected."),
    ARTICLE_METADATA_DEBIT_BUILD_FAILED("Failed to build ArticleMetadata for the debit entry."),
    ARTICLE_METADATA_CREDIT_BUILD_FAILED("Failed to build ArticleMetadata for the credit entry."),
    CALCULATION_CONTEXT_COMPONENT_MISSING("Primary article component ''{0}'' missing in calculation context."),
    SANCTIONED_LOAN_NOT_FOUND_FOR_FACILITY("Sanctioned loan not found for facility {0}"),
    SANCTIONED_LOAN_NOT_FOUND("Sanctioned loan not found"),
    NO_ARTICLE_COMPONENTS_ADDED("No article components added"),
    ARTICLE_METADATA_BUILD_FAILED("Failed to build article metadata.");

    private final String messageFormat;

    TradeLoanFacilityLocalizedMessageCodes(String defaultMessageFormat) {
        this.messageFormat = defaultMessageFormat;
    }

    @Override
    @NonNull
    public String getDefaultMessageFormat() {
        return messageFormat;
    }
}
