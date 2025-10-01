package ir.dotin.loan.trade.core.application.service.irregulardisbursement.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IrregularDisbursementErrorCodes implements LocalizedMessage<IrregularDisbursementErrorCodes> {
    FACILITY_NOT_FOUND("Loan facility with ID ''{0}'' not found"),
    INVALID_DISBURSEMENT_METHOD("This facility does not use STAGED_IRREGULAR method. Current method: {0}"),
    INVALID_REQUESTED_AMOUNT("Invalid requested amount: {0}"),
    IRREGULAR_DISBURSEMENT_FAILED("Failed to complete irregular disbursement: {0}");

    private final String defaultMessageFormat;
}
