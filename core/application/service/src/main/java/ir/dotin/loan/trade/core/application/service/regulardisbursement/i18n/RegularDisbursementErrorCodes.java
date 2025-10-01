package ir.dotin.loan.trade.core.application.service.regulardisbursement.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RegularDisbursementErrorCodes implements LocalizedMessage<RegularDisbursementErrorCodes> {
    FACILITY_NOT_FOUND("Loan facility with ID ''{0}'' not found"),
    INVALID_DISBURSEMENT_METHOD("This facility does not use STAGED_REGULAR method. Current method: {0}"),
    INVALID_TRANCHE_AMOUNT("Invalid tranche amount: {0}"),
    REGULAR_DISBURSEMENT_FAILED("Failed to complete regular disbursement: {0}");

    private final String defaultMessageFormat;
}
