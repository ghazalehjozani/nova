package ir.dotin.loan.trade.core.application.query.loanfacility.i18n;

import ir.dotin.platform.pangaea.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum LoanFacilityQueryErrorCodes implements LocalizedMessage<LoanFacilityQueryErrorCodes> {
    FACILITY_NOT_FOUND("Facility with ID {0} not found");

    private final String defaultMessageFormat;
}
