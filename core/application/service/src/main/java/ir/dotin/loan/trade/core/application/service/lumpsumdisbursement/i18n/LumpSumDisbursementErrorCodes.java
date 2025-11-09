package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LumpSumDisbursementErrorCodes implements LocalizedMessage<LumpSumDisbursementErrorCodes> {
    FACILITY_NOT_FOUND("Loan facility with ID ''{0}'' not found"),
    INVALID_DISBURSEMENT_METHOD("This facility does not use LUMP_SUM method. Current method: {0}"),
    INVALID_AMOUNT("Invalid disbursement amount: {0}"),
    LUMP_SUM_DISBURSEMENT_FAILED("Failed to complete lump sum disbursement: {0}"),
    LOAN_TYPE_NOT_FOUND("Loan type {0} not found for facility: {1}"),
    LOAN_ARRANGEMENT_NOT_FOUND("Loan arrangement {0} not found for facility: {1}"),
    SANCTIONED_LOAN_NOT_FOUND("Sanctioned loan not found for facility {0}"),
    TRANSACTION_CREATION_FAILED("Failed to create disbursement transactions for facility {0}"),
    TRANSACTION_POSTING_FAILED("Failed to post transaction for facility {0}"),
    INSTALLMENT_SCHEDULE_NOT_FOUND("Installment schedule not found for facility {0}");

    private final String defaultMessageFormat;
}
