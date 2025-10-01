package ir.dotin.loan.trade.core.application.service.openfacilitycase.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum OpenFacilityCaseErrorCodes implements LocalizedMessage<OpenFacilityCaseErrorCodes> {
    FACILITY_ALREADY_EXISTS("Facility with ID {0} already exists"),
    INVALID_LOAN_TYPE("Invalid loan type ID: {0}"),
    INVALID_AMOUNT("Invalid requested amount: {0}"),
    INVALID_DURATION("Invalid duration: {0} days"),
    INVALID_LOAN_ARRANGEMENT("Invalid loan arrangement ID: {0}"),
    ;

    private final String defaultMessageFormat;
}
