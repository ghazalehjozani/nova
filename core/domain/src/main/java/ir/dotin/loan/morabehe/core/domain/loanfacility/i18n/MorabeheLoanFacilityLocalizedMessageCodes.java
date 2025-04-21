package ir.dotin.loan.morabehe.core.domain.loanfacility.i18n;

import ir.dotin.platform.domain.common.i18n.LocalizedMessage;
import ir.dotin.loan.morabehe.core.domain.MorabeheLoanValidationConstants;

public enum MorabeheLoanFacilityLocalizedMessageCodes
        implements LocalizedMessage<MorabeheLoanFacilityLocalizedMessageCodes> {
    BUILDER_VALIDATION_FAILED(
            "builder.validation_failed", "Failed to build MorabeheLoanApplication due to validation errors."),
    LOAN_TYPE_ID_REQUIRED("loan_type_id_required", "Morabehe Loan Type ID is required."),
    LOAN_RULE_ID_REQUIRED("loan_rule_id_required", "Morabehe Loan Rule ID is required."),
    ARRANGEMENT_FETCH_FAILED("arrangement.fetch_failed", "Failed to fetch Loan Arrangement details for ID: {0}"),
    INTEREST_CALCULATION_FAILED("interest.calculation_failed", "Failed to calculate interest: {0}"),
    TRANSACTION_POSTING_FAILED("transaction.posting_failed", "Failed to post transaction."),
    UNKNOWN_STRATEGY_TYPE("strategy.unknown_type", "Cannot determine purpose code for unknown strategy type: {0}"),
    LOAN_TOPIC_RESOLUTION_FAILED(
            "loantopic.resolution_failed",
            "Failed to resolve primary loan topic for LoanType ID {0} and EconomicSector {1}"),
    CANNOT_POST_DISBURSE_TRANSACTION(
            "transaction.disbursement.invalid_state",
            "Cannot post disbursement transactions while facility is in state: {0}"),
    UNEXPECTED_ERROR("system.unexpected_error", "An unexpected error occurred: {0}"),

    INTEREST_FORMULA_MISSING(
            "interest.formula.missing", "Interest calculation failed: Formula is missing in the InterestPolicy."),
    INTEREST_FORMULA_EVALUATION_FAILED(
            "interest.formula.evaluation_failed", "Interest calculation failed during formula evaluation"),
    INTEREST_RESULT_CONVERSION_FAILED(
            "interest.result.conversion_failed", "Interest calculation failed: Could not convert result to Money: {0}"),
    INTEREST_CALCULATION_UNEXPECTED_ERROR(
            "interest.calculation.unexpected_error", "Unexpected error during interest calculation: {0}");

    private final String constructedKey;
    private final String messageFormat;

    MorabeheLoanFacilityLocalizedMessageCodes(String keySuffix, String defaultMessageFormat) {
        this.constructedKey = MorabeheLoanValidationConstants.LOAN_APPLICATION_KEY_PREFIX + keySuffix;
        this.messageFormat = defaultMessageFormat;
    }

    @Override
    public String getKey() {
        return constructedKey;
    }

    @Override
    public String getDefaultMessageFormat() {
        return messageFormat;
    }
}
