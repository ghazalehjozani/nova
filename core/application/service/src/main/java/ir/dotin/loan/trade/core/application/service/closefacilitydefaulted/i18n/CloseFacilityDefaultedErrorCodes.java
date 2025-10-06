package ir.dotin.loan.trade.core.application.service.closefacilitydefaulted.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CloseFacilityDefaultedErrorCodes implements LocalizedMessage<CloseFacilityDefaultedErrorCodes> {
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    FACILITY_NOT_ACTIVE("Facility {0} is not in active state"),
    INVALID_STATE("Facility {0} is in invalid state for default closure"),
    MISSING_REASON("Default reason is required");

    private final String defaultMessageFormat;
}
