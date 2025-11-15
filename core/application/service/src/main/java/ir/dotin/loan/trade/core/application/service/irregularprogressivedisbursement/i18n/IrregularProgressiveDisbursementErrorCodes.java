package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IrregularProgressiveDisbursementErrorCodes
        implements LocalizedMessage<IrregularProgressiveDisbursementErrorCodes> {
    FACILITY_NOT_FOUND("Loan facility with ID ''{0}'' not found"),
    INVALID_DISBURSEMENT_METHOD("This facility does not use IRREGULAR_PROGRESSIVE method. Current method: {0}"),
    INVALID_TRANCHE_AMOUNT("Invalid tranche amount: {0}"),
    DISBURSEMENT_EXCEEDS_CAPACITY("Tranche amount {0} exceeds remaining capacity {1}"),
    INVALID_FACILITY_STATUS("Cannot disburse in facility status: {0}"),
    LOAN_TYPE_NOT_FOUND("Loan type {0} not found for facility: {1}"),
    LOAN_ARRANGEMENT_NOT_FOUND("Loan arrangement {0} not found for facility: {1}"),
    INSTALLMENT_SCHEDULE_NOT_FOUND("Installment schedule not found for facility {0}"),
    SCHEDULE_RECALCULATION_FAILED("Failed to recalculate installment schedule: {0}"),
    TRANSACTION_CREATION_FAILED("Failed to create disbursement transactions for facility {0}"),
    TRANSACTION_POSTING_FAILED("Failed to post transaction for facility {0}"),
    INVALID_BRANCH_CODE("Invalid branch code: {0}"),
    NO_UNPAID_INSTALLMENTS("No unpaid installments available for recalculation"),
    INVALID_SCHEDULE_STATUS_FOR_FIRST_DISBURSEMENT(
            "First disbursement requires schedule in DRAFT status, but found: {0}"),
    INVALID_SCHEDULE_STATUS_FOR_SUBSEQUENT_DISBURSEMENT(
            "Subsequent disbursement requires schedule in ACTIVE status, but found: {0}"),
    RECALC_INVALID_PLAN_COUNT("Custom plan installment count ({0}) must match unpaid installments ({1})");

    private final String defaultMessageFormat;
}
