package ir.dotin.loan.trade.core.application.service.rejectfacility.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum RejectFacilityErrorCodes implements LocalizedMessage<RejectFacilityErrorCodes> {
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    FACILITY_NOT_SUBMITTED("Facility {0} has not been submitted for approval"),
    INVALID_STATE("Facility {0} is in invalid state for rejection"),
    MISSING_REASON("Rejection reason is required");

    private final String defaultMessageFormat;
}
