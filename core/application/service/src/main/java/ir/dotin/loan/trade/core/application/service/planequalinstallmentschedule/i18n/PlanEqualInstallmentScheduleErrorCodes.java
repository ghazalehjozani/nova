package ir.dotin.loan.trade.core.application.service.planequalinstallmentschedule.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum PlanEqualInstallmentScheduleErrorCodes implements LocalizedMessage<PlanEqualInstallmentScheduleErrorCodes> {
    SCHEDULE_ALREADY_EXISTS("Installment schedule with ID {0} already exists"),
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    INVALID_INSTALLMENT_COUNT("Invalid number of installments: {0}"),
    INVALID_PERIOD("Invalid installment period: {0} days"),
    INVALID_INTEREST_RATE("Invalid interest rate: {0}"),
    ARRANGEMENT_NOT_FOUND("Arrangement with ID {0} not found");

    private final String defaultMessageFormat;
}
