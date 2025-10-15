package ir.dotin.loan.trade.core.application.service.plangradualinstallmentschedule.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum PlanUnequalInstallmentScheduleErrorCodes
        implements LocalizedMessage<PlanUnequalInstallmentScheduleErrorCodes> {
    SCHEDULE_ALREADY_EXISTS("Installment schedule with ID {0} already exists"),
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    EMPTY_INSTALLMENTS("Installment list cannot be empty"),
    INVALID_TOTAL_AMOUNT("Sum of installment amounts {0} does not match total loan amount {1}"),
    INVALID_INTEREST_RATE("Invalid interest rate: {0}"),
    DUPLICATE_DUE_DATE("Duplicate due date found: {0}"),
    ARRANGEMENT_NOT_FOUND("Arrangement with ID {0} not found");

    private final String defaultMessageFormat;
}
