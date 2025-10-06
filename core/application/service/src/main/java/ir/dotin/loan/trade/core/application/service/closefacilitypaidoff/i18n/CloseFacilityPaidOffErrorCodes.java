package ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CloseFacilityPaidOffErrorCodes implements LocalizedMessage<CloseFacilityPaidOffErrorCodes> {
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    FACILITY_NOT_ACTIVE("Facility {0} is not in active state"),
    INVALID_STATE("Facility {0} is in invalid state for closing"),
    OUTSTANDING_BALANCE("Facility {0} has outstanding balance");

    private final String defaultMessageFormat;
}
