package ir.dotin.loan.trade.core.application.service.cancelfacility.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CancelFacilityErrorCodes implements LocalizedMessage<CancelFacilityErrorCodes> {
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    INVALID_STATE("Facility {0} is in invalid state for cancellation"),
    MISSING_REASON("Cancellation reason is required");

    private final String defaultMessageFormat;
}
