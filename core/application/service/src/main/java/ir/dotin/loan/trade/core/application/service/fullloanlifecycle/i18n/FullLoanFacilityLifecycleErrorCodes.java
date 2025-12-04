package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum FullLoanFacilityLifecycleErrorCodes implements LocalizedMessage<FullLoanFacilityLifecycleErrorCodes> {
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    LOAN_TYPE_NOT_FOUND("Loan type {0} not found"),
    LOAN_ARRANGEMENT_NOT_FOUND("Loan arrangement {0} not found"),
    INVALID_DISBURSEMENT_METHOD("Unsupported disbursement method: {0}"),
    ORIGINATION_FAILED("Failed to originate facility: {0}"),
    APPROVAL_SUBMISSION_FAILED("Failed to submit facility for approval: {0}"),
    APPROVAL_FAILED("Failed to approve facility: {0}"),
    CONTRACT_ISSUANCE_FAILED("Failed to issue contract: {0}"),
    DISBURSEMENT_FAILED("Failed to disburse facility: {0}"),
    TRANSACTION_POSTING_FAILED("Failed to post transaction: {0}"),
    COMPENSATION_FAILED("Failed to compensate step {0}: {1}"),
    SCHEDULE_NOT_FOUND("Schedule with ID {0} not found"),
    NO_ACTIVE_SCHEDULE("No active schedule for loan {0}"),
    INVALID_STATE_FOR_COMPENSATION("Facility {0} is in invalid state for compensation: {1}"),
    SANCTIONED_LOAN_NOT_FOUND("Sanctioned loan not found for facility {0}"),
    SCHEDULE_ACTIVATION_FAILED("Failed to activate installment schedule: {0}"),
    REVERT_FAILED("Failed to revert facility lifecycle: {0}");

    private final String defaultMessageFormat;
}
