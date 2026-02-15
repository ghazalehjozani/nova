package ir.dotin.loan.trade.core.application.service.collectinstallment.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CollectInstallmentErrorCodes implements LocalizedMessage<CollectInstallmentErrorCodes> {

    SCHEDULE_NOT_FOUND("Installment schedule not found: {0}"),
    FACILITY_NOT_FOUND("Loan facility not found: {0}"),
    FILE_NUMBER_NOT_FOUND("No facility found for file number: {0}"),
    INVALID_CURRENCY("Currency mismatch: {0} vs {1}"),
    PAYMENT_FAILED("Failed to collect installment: {0}");

    private final String defaultMessageFormat;
}
