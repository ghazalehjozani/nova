package ir.dotin.loan.trade.core.application.service.shared.error;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.pangaea.commons.core.error.ErrorCategory;
import ir.dotin.platform.pangaea.commons.core.error.PlatformErrorCategory;
import ir.dotin.platform.pangaea.commons.core.error.ProductErrorCode;
import ir.dotin.loan.baseloan.core.domain.shared.error.LoanErrorCategory;

/*
 * Consolidated from: AddFacilityCollateralErrorCodes, ApproveFacilityErrorCodes,
 *                    CancelFacilityErrorCodes, CloseFacilityDefaultedErrorCodes,
 *                    InstallmentScheduleQueryErrorCodes, LoanFacilityQueryErrorCodes.
 * Duplicates removed: FACILITY_NOT_FOUND, LOAN_ARRANGEMENT_NOT_FOUND,
 *                     INVALID_STATE, MISSING_REASON.
 *
 * Sequences per category for this file:
 *   FACILITY_LIFECYCLE (31): 81–100,  reserved — (1–80 used by other files)
 *   COLLATERAL         (36): 31–50,   reserved — (1–30 used by other files)
 *   SANCTION           (37): 21–40,   reserved — (1–20 used by LoanFacilityErrors)
 *   INSTALLMENT_SCHEDULE(32): 41–50,  reserved — (1–40 used by InstallmentScheduleErrors)
 *   STATE_CONFLICT     (08): 56–70,   reserved —
 *   VALIDATION         (01): 291–300, reserved —
 *   DATA_ACCESS        (06): 6–10,    reserved — (1–5 used by InstallmentScheduleErrors)
 *   INTEGRATION        (03): 11–15,   reserved — (1–10 used by CoreBankingErrors)
 */
public enum TradeLoanApplicationServiceErrors implements ProductErrorCode<TradeLoanApplicationServiceErrors> {

    // ── FACILITY_LIFECYCLE (31) — shared lookup & lifecycle ──────────────────────

    FACILITY_NOT_FOUND(LoanErrorCategory.FACILITY_LIFECYCLE, 81, "Facility with ID {0} not found"),
    LOAN_ARRANGEMENT_NOT_FOUND(LoanErrorCategory.FACILITY_LIFECYCLE, 82, "Loan arrangement with ID {0} not found"),
    INSTALLMENT_SCHEDULE_NOT_FOUND(
            LoanErrorCategory.FACILITY_LIFECYCLE, 83, "Installment schedule with ID {0} not found"),
    LOAN_TYPE_NOT_FOUND(LoanErrorCategory.FACILITY_LIFECYCLE, 84, "Loan type with ID {0} not found"),

    // ── COLLATERAL (36) — add-facility-collateral ───────────────────────────────

    INVALID_COLLATERAL_TYPE(LoanErrorCategory.COLLATERAL, 31, "Invalid collateral type: {0}"),
    INVALID_COLLATERAL_VALUE(LoanErrorCategory.COLLATERAL, 32, "Invalid collateral number: {0}"),
    INSUFFICIENT_COLLATERAL_VALUE(
            LoanErrorCategory.COLLATERAL, 33, "Collateral value {0} is less than the required amount {1}"),
    COLLATERAL_VALIDATION_FAILED(LoanErrorCategory.COLLATERAL, 34, "Collateral is not valid"),
    COLLATERAL_DETAILS_NOT_FOUND(LoanErrorCategory.COLLATERAL, 35, "Collateral details with serial {0} not found"),
    ADD_COLLATERAL_PROCESS_COULD_NOT_COMPLETE(
            LoanErrorCategory.COLLATERAL, 36, "Add collateral process could not complete for facility"),

    // ── SANCTION (37) — approve-facility ─────────────────────────────────────────

    INVALID_AMOUNT(LoanErrorCategory.SANCTION, 21, "Invalid approved amount: {0}"),
    SANCTION_SERIAL_REQUIRED_FOR_MANUAL_APPROVAL(
            LoanErrorCategory.SANCTION, 22, "Sanction serial is required for manual approval"),
    SANCTION_SERIAL_NOT_ALLOWED_FOR_AUTO_APPROVAL(
            LoanErrorCategory.SANCTION, 23, "Sanction serial is not allowed for auto approval"),
    AUTO_APPROVAL_NOT_ENABLED(
            LoanErrorCategory.SANCTION, 24, "Auto approval is not enabled for this application channel {0}"),
    MANUAL_APPROVAL_NOT_ALLOWED(
            LoanErrorCategory.SANCTION, 25, "Manual approval is not allowed for this application channel {0}"),
    INVALID_SANCTION_DETAILS(LoanErrorCategory.SANCTION, 26, "Invalid sanction details: {0}"),
    CONFIRM_TYPE_NOT_ALLOWED(
            LoanErrorCategory.SANCTION, 27, "Confirm type {0} is not allowed based on loan arrangement {1}"),
    SANCTIONED_LOAN_NOT_FOUND(LoanErrorCategory.SANCTION, 28, "Sanctioned loan not found for facility {0}"),

    // ── STATE_CONFLICT (08) — lifecycle state errors ────────────────────────────

    FACILITY_NOT_SUBMITTED(
            PlatformErrorCategory.STATE_CONFLICT, 56, "Facility {0} has not been submitted for approval"),
    FACILITY_INVALID_STATE_FOR_APPROVAL(
            PlatformErrorCategory.STATE_CONFLICT, 57, "Facility {0} is in invalid state for approval"),
    FACILITY_INVALID_STATE_FOR_CANCELLATION(
            PlatformErrorCategory.STATE_CONFLICT, 58, "Facility {0} is in invalid state for cancellation"),
    FACILITY_NOT_ACTIVE(PlatformErrorCategory.STATE_CONFLICT, 59, "Facility {0} is not in active state"),
    FACILITY_INVALID_STATE_FOR_DEFAULT_CLOSURE(
            PlatformErrorCategory.STATE_CONFLICT, 60, "Facility {0} is in invalid state for default closure"),
    INVALID_SCHEDULE_STATUS_FOR_FIRST_DISBURSEMENT(
            PlatformErrorCategory.STATE_CONFLICT,
            61,
            "First disbursement requires schedule in DRAFT status, but found: {0}"),
    INVALID_SCHEDULE_STATUS_FOR_SUBSEQUENT_DISBURSEMENT(
            PlatformErrorCategory.STATE_CONFLICT,
            62,
            "Subsequent disbursement requires schedule in ACTIVE status, but found: {0}"),

    // ── DATA_ACCESS (06) ────────────────────────────────────────────────────────

    DUPLICATE_COLLATERAL(PlatformErrorCategory.DATA_ACCESS, 6, "Collateral {0} already exists for facility"),
    DUPLICATE_LOAN_TYPE(PlatformErrorCategory.DATA_ACCESS, 7, "Loan Type {0} already exists for facility"),
    DUPLICATE_LOAN_ARRANGEMENT(
            PlatformErrorCategory.DATA_ACCESS, 8, "Loan Arrangement {0} already exists for facility"),

    // ── VALIDATION (01) ─────────────────────────────────────────────────────────

    APPLICATION_NUMBER_MISSING(PlatformErrorCategory.VALIDATION, 291, "Application number is missing"),
    MISSING_REASON(PlatformErrorCategory.VALIDATION, 292, "Reason is required"),
    DUPLICATE_CODE(PlatformErrorCategory.VALIDATION, 293, "Code {0} already exists"),
    INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE(
            PlatformErrorCategory.VALIDATION, 294, "Economic sector for loan type {0} does not exist"),
    INVALID_DISBURSEMENT_METHOD(
            PlatformErrorCategory.VALIDATION, 295, "Current method: {0} in not valid for disbursement"),
    INVALID_BRANCH_CODE(PlatformErrorCategory.VALIDATION, 296, "Branch code {0} is not valid"),
    FORMULA_NOT_EXIST(PlatformErrorCategory.VALIDATION, 297, "Formula with id {0} not exist"),

    // ── INTEGRATION (03) ────────────────────────────────────────────────────────

    SANCTION_DETAILS_SERVICE_NOT_IMPLEMENTED(
            PlatformErrorCategory.INTEGRATION, 11, "Sanction details service is not implemented for serial {0}");

    private final ErrorCategory category;
    private final int sequence;
    private final String defaultMessageFormat;

    TradeLoanApplicationServiceErrors(ErrorCategory category, int sequence, String defaultMessageFormat) {
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
