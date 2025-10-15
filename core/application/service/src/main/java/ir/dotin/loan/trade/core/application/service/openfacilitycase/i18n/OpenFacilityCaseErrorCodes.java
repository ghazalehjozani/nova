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
    INSTALLMENT_SCHEDULE_IS_MANDATORY_IN_GRADUAL("Installment schedule is mandatory for gradual payment"),
    FACILITY_CREATION_FAILED("Failed to create facility: {0}"),
    FACILITY_SAVE_FAILED("Failed to save facility: {0}"),
    SCHEDULE_DISPATCH_FAILED("Failed to dispatch installment schedule command: {0}");

    private final String defaultMessageFormat;
}
