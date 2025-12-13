package ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum OriginateLoanFacilityErrorCodes implements LocalizedMessage<OriginateLoanFacilityErrorCodes> {
    FACILITY_ALREADY_EXISTS("Facility with ID {0} already exists"),
    INVALID_LOAN_TYPE("Invalid loan type ID: {0}"),
    FACILITY_NOT_FOUND("Facility with ID {0} not found"),
    INVALID_AMOUNT("Invalid requested amount: {0}"),
    INVALID_DURATION("Invalid duration: {0} days"),
    BRANCH_CODE_REQUIRED("Branch code required"),
    INVALID_LOAN_ARRANGEMENT("Invalid loan arrangement Code: {0}"),
    INSTALLMENT_SCHEDULE_IS_MANDATORY_IN_GRADUAL("Installment schedule is mandatory for gradual payment"),
    FACILITY_CREATION_FAILED("Failed to create facility: {0}"),
    FACILITY_SAVE_FAILED("Failed to save facility: {0}"),
    FACILITY_PERSISTENCE_FAILED("Failed to persist facility: {0}"),
    INVALID_DEPOSIT_CURRENCY("Invalid deposit currency: {0}"),
    INVALID_CREDITOR_DEPOSIT("Invalid creditor deposit: {0}"),
    INVALID_DEBTOR_DEPOSIT("Invalid debtor deposit: {0}"),
    INSTALLMENT_COUNT_CANNOT_BE_EMPTY("Installment count cannot be empty"),
    DUPLICATE_APPLICATION_NUMBER("Application number {0} already exists"),
    DISBURSE_DESTINATION_DEPOSIT_IS_CLOSED("Disburse destination deposit is closed. deposit: {0}. "),
    INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE("Economic sector {0} is not allowed for loan type {1}"),
    APPLICATION_NUMBER_MISMATCH("Application number mismatch. Command provided: {0}, Generated: {1}"),
    APPLICATION_NUMBER_CREATION_FAILED("Failed to create application number: {0}"),
    ECONOMIC_SECTOR_IS_PARENT("The selected economic sector {0} is a parent sector. Please select a child sector.");

    private final String defaultMessageFormat;
}
