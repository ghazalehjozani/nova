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
    INVALID_AMOUNT("Invalid approved amount: {0}"),
    SANCTION_SERIAL_REQUIRED_FOR_MANUAL_APPROVAL("Sanction serial is required for manual approval"),
    SANCTION_SERIAL_NOT_ALLOWED_FOR_AUTO_APPROVAL("Sanction serial is not allowed for auto approval"),
    AUTO_APPROVAL_NOT_ENABLED("Auto approval is not enabled for this application channel {0}"),
    MANUAL_APPROVAL_NOT_ALLOWED("Manual approval is not allowed for this application channel {0}"),
    SANCTION_DETAILS_SERVICE_NOT_IMPLEMENTED("Sanction details service is not implemented for serial {0}"),
    INVALID_SANCTION_DETAILS("Invalid sanction details: {0}");

    private final String defaultMessageFormat;
}
