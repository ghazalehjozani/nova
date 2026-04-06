package ir.dotin.loan.trade.core.domain.loanfacility.error;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.error.ErrorCategory;
import ir.dotin.platform.commons.core.error.PlatformErrorCategory;
import ir.dotin.platform.commons.core.error.ProductErrorCode;
import ir.dotin.loan.baseloan.core.domain.shared.error.LoanErrorCategory;
import ir.dotin.loan.trade.core.domain.shared.error.TradeLoanErrorCategory;

/*
 * Sequences per category for this file:
 *   TRADE_FACILITY         (52): 1–30,    reserved 31–99
 *   INTEREST_CALCULATION   (35): 11–25,   reserved — (1–10 used by InstallmentScheduleErrors)
 *   FACILITY_LIFECYCLE     (31): 61–80,   reserved — (1–10 Inst, 11–60 LoanFacility)
 *   ACCOUNTING             (39): 96–110,  reserved — (1–95 used by LoanValidationErrors)
 *   VALIDATION             (01): 271–290, reserved —
 */
public enum TradeLoanFacilityErrors implements ProductErrorCode<TradeLoanFacilityErrors> {

    // ── TRADE_FACILITY (52) — trade-specific processing ─────────────────────────

    BUILDER_VALIDATION_FAILED(
            TradeLoanErrorCategory.TRADE_FACILITY, 1, "Failed to build application due to validation errors."),
    LOAN_TYPE_ID_REQUIRED(TradeLoanErrorCategory.TRADE_FACILITY, 2, "Trade Loan Type ID is required."),
    LOAN_RULE_ID_REQUIRED(TradeLoanErrorCategory.TRADE_FACILITY, 3, "Trade Loan Rule ID is required."),
    ARRANGEMENT_FETCH_FAILED(
            TradeLoanErrorCategory.TRADE_FACILITY, 4, "Failed to fetch Loan Arrangement details for ID: {0}"),
    TRANSACTION_POSTING_FAILED(TradeLoanErrorCategory.TRADE_FACILITY, 5, "Failed to post transaction."),
    ALL_DISBURSEMENT_INPUTS_REQUIRED(TradeLoanErrorCategory.TRADE_FACILITY, 6, "All disbursement inputs are required"),
    UNKNOWN_STRATEGY_TYPE(
            TradeLoanErrorCategory.TRADE_FACILITY, 7, "Cannot determine purpose code for unknown strategy type: {0}"),
    LOAN_TOPIC_RESOLUTION_FAILED(
            TradeLoanErrorCategory.TRADE_FACILITY,
            8,
            "Failed to resolve primary loan topic for LoanType ID {0} and EconomicSector {1}"),

    // ── INTEREST_CALCULATION (35) — trade-specific interest ─────────────────────

    INTEREST_CALCULATION_FAILED(LoanErrorCategory.INTEREST_CALCULATION, 11, "Failed to calculate interest: {0}"),
    INTEREST_FORMULA_MISSING(
            LoanErrorCategory.INTEREST_CALCULATION,
            12,
            "Interest calculation failed: Formula is missing in the InterestPolicy."),
    INTEREST_FORMULA_EVALUATION_FAILED(
            LoanErrorCategory.INTEREST_CALCULATION, 13, "Interest calculation failed during formula evaluation"),
    INTEREST_RESULT_CONVERSION_FAILED(
            LoanErrorCategory.INTEREST_CALCULATION,
            14,
            "Interest calculation failed: Could not convert result to Money: {0}"),
    INTEREST_CALCULATION_UNEXPECTED_ERROR(
            LoanErrorCategory.INTEREST_CALCULATION, 15, "Unexpected error during interest calculation: {0}"),
    INCREMENTAL_INTEREST_CANNOT_BE_NEGATIVE(
            LoanErrorCategory.INTEREST_CALCULATION,
            16,
            "New schedule total interest ({0}) cannot be less than current schedule total interest ({1})"),

    // ── FACILITY_LIFECYCLE (31) — trade-specific lifecycle ───────────────────────

    CANNOT_POST_DISBURSE_TRANSACTION(
            LoanErrorCategory.FACILITY_LIFECYCLE,
            61,
            "Cannot post disbursement transactions while facility is in state: {0}"),
    SANCTIONED_LOAN_NOT_FOUND_FOR_FACILITY(
            LoanErrorCategory.FACILITY_LIFECYCLE, 62, "Sanctioned loan not found for facility {0}"),
    SANCTIONED_LOAN_NOT_FOUND(LoanErrorCategory.FACILITY_LIFECYCLE, 63, "Sanctioned loan not found"),
    UNEXPECTED_ERROR(LoanErrorCategory.FACILITY_LIFECYCLE, 64, "An unexpected error occurred: {0}"),

    // ── ACCOUNTING (39) — trade-specific accounting ─────────────────────────────

    STRATEGY_RETURNED_NO_ITEMS(
            LoanErrorCategory.ACCOUNTING, 96, "Strategy did not return any items when items were expected."),
    ARTICLE_METADATA_DEBIT_BUILD_FAILED(
            LoanErrorCategory.ACCOUNTING, 97, "Failed to build ArticleMetadata for the debit entry."),
    ARTICLE_METADATA_CREDIT_BUILD_FAILED(
            LoanErrorCategory.ACCOUNTING, 98, "Failed to build ArticleMetadata for the credit entry."),
    CALCULATION_CONTEXT_COMPONENT_MISSING(
            LoanErrorCategory.ACCOUNTING, 99, "Primary article component ''{0}'' missing in calculation context."),
    NO_ARTICLE_COMPONENTS_ADDED(LoanErrorCategory.ACCOUNTING, 100, "No article components added"),
    ARTICLE_METADATA_BUILD_FAILED(LoanErrorCategory.ACCOUNTING, 101, "Failed to build article metadata."),

    // ── VALIDATION (01) ─────────────────────────────────────────────────────────

    FACILITY_CANNOT_BE_NULL(PlatformErrorCategory.VALIDATION, 271, "Facility cannot be null"),
    LOAN_TYPE_CANNOT_BE_NULL(PlatformErrorCategory.VALIDATION, 272, "Loan type cannot be null"),
    BRANCH_CODE_CANNOT_BE_NULL(PlatformErrorCategory.VALIDATION, 273, "Branch code cannot be null"),
    POST_TITLE_CANNOT_BE_NULL(PlatformErrorCategory.VALIDATION, 274, "Post title cannot be null"),
    CALCULATION_CONTEXT_LOAN_APPLICATION_MISSING_IN_FACILITY(
            PlatformErrorCategory.VALIDATION, 275, "LoanApplication is missing within the provided facility."),
    CALCULATION_CONTEXT_ECONOMIC_SECTOR_MISSING(
            PlatformErrorCategory.VALIDATION, 276, "EconomicSector is missing from the LoanApplication."),
    CALCULATION_CONTEXT_APPLICATION_NUMBER_MISSING(
            PlatformErrorCategory.VALIDATION,
            277,
            "ApplicationNumber is missing from the LoanApplication, cannot create PostTitle."),
    CALCULATION_CONTEXT_POST_TITLE_CREATION_FAILED(
            PlatformErrorCategory.VALIDATION, 278, "Failed to create PostTitle from application number: {0}.");

    private final ErrorCategory category;
    private final int sequence;
    private final String defaultMessageFormat;

    TradeLoanFacilityErrors(ErrorCategory category, int sequence, String defaultMessageFormat) {
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
