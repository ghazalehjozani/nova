package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum AddFacilityCollateralErrorCodes implements LocalizedMessage<AddFacilityCollateralErrorCodes> {
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    INVALID_COLLATERAL_TYPE("Invalid collateral type: {0}"),
    INVALID_COLLATERAL_VALUE("Invalid collateral number: {0}"),
    DUPLICATE_COLLATERAL("Collateral {0} already exists for facility"),
    LOAN_ARRANGEMENT_NOT_FOUND("Loan arrangement with ID {0} not found"),
    INSUFFICIENT_COLLATERAL_VALUE("Collateral value {0} is less than the required amount {1}"),
    COLLATERAL_VALIDATION_FAILED("Collateral is not valid");

    private final String defaultMessageFormat;
}
