package ir.dotin.loan.trade.core.application.service.approvefacility.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum ApproveFacilityErrorCodes implements LocalizedMessage<ApproveFacilityErrorCodes> {
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    LOAN_ARRANGEMENT_NOT_FOUND("Loan Arrangement with ID {0} not found"),
    FACILITY_NOT_SUBMITTED("Facility {0} has not been submitted for approval"),
    INVALID_STATE("Facility {0} is in invalid state for approval"),
    INVALID_AMOUNT("Invalid approved amount: {0}");

    private final String defaultMessageFormat;
}
