package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LumpSumDisbursementErrorCodes implements LocalizedMessage<LumpSumDisbursementErrorCodes> {
    FACILITY_NOT_FOUND("Loan facility with ID ''{0}'' not found"),
    INVALID_DISBURSEMENT_METHOD("This facility does not use LUMP_SUMP method. Current method: {0}"),
    INVALID_AMOUNT("Invalid disbursement amount: {0}"),
    LUMP_SUM_DISBURSEMENT_FAILED("Failed to complete lump sum disbursement: {0}");

    private final String defaultMessageFormat;
}
