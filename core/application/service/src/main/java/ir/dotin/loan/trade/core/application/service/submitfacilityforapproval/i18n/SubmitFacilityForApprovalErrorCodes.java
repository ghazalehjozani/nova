package ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.i18n;

import ir.dotin.platform.pangaea.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum SubmitFacilityForApprovalErrorCodes implements LocalizedMessage<SubmitFacilityForApprovalErrorCodes> {
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    FACILITY_ALREADY_SUBMITTED("Facility {0} is already submitted"),
    INVALID_STATE("Facility {0} is in invalid state for submission");

    private final String defaultMessageFormat;
}
