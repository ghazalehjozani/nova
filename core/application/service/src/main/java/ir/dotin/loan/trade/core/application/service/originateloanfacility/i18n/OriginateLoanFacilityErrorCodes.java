package ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.pangaea.commons.core.error.ErrorCategory;
import ir.dotin.platform.pangaea.commons.core.error.ProductErrorCode;
import ir.dotin.loan.trade.core.domain.shared.error.TradeLoanErrorCategory;

/*
 * TRADE_SERVICE (54) sub-range allocation:
 *   Originate (this file):                001–025, 029–032
 *   Submit (SubmitFacilityForApprovalErrorCodes): 026–028
 *   Reserved:                             033–999
 *
 * Originate constants are assigned explicit sequences 1..N in declaration order
 * (numericCode = 54*1000 + sequence). This file currently declares 24 constants
 * in 001–024; 025 remains reserved within the Originate sub-range. Party
 * eligibility-screening codes (added for applicant/guarantor blacklist, incapacity,
 * graylist and active-customer gating) take the contiguous 029–032 band, jumping past
 * the Submit band 026–028 to avoid renumbering Submit.
 */
public enum OriginateLoanFacilityErrorCodes implements ProductErrorCode<OriginateLoanFacilityErrorCodes> {
    FACILITY_ALREADY_EXISTS(TradeLoanErrorCategory.TRADE_SERVICE, 1, "Facility with ID {0} already exists"),
    INVALID_LOAN_TYPE(TradeLoanErrorCategory.TRADE_SERVICE, 2, "Invalid loan type ID: {0}"),
    FACILITY_NOT_FOUND(TradeLoanErrorCategory.TRADE_SERVICE, 3, "Facility with ID {0} not found"),
    INVALID_AMOUNT(TradeLoanErrorCategory.TRADE_SERVICE, 4, "Invalid requested amount: {0}"),
    INVALID_DURATION(TradeLoanErrorCategory.TRADE_SERVICE, 5, "Invalid duration: {0} days"),
    BRANCH_CODE_REQUIRED(TradeLoanErrorCategory.TRADE_SERVICE, 6, "Branch code required"),
    INVALID_LOAN_ARRANGEMENT(TradeLoanErrorCategory.TRADE_SERVICE, 7, "Invalid loan arrangement Code: {0}"),
    INSTALLMENT_SCHEDULE_IS_MANDATORY_IN_GRADUAL(
            TradeLoanErrorCategory.TRADE_SERVICE, 8, "Installment schedule is mandatory for gradual payment"),
    FACILITY_CREATION_FAILED(TradeLoanErrorCategory.TRADE_SERVICE, 9, "Failed to create facility: {0}"),
    FACILITY_SAVE_FAILED(TradeLoanErrorCategory.TRADE_SERVICE, 10, "Failed to save facility: {0}"),
    FACILITY_PERSISTENCE_FAILED(TradeLoanErrorCategory.TRADE_SERVICE, 11, "Failed to persist facility: {0}"),
    INVALID_DEPOSIT_CURRENCY(TradeLoanErrorCategory.TRADE_SERVICE, 12, "Invalid deposit currency: {0}"),
    INVALID_CREDITOR_DEPOSIT(TradeLoanErrorCategory.TRADE_SERVICE, 13, "Invalid creditor deposit: {0}"),
    INVALID_DEBTOR_DEPOSIT(TradeLoanErrorCategory.TRADE_SERVICE, 14, "Invalid debtor deposit: {0}"),
    INSTALLMENT_COUNT_CANNOT_BE_EMPTY(TradeLoanErrorCategory.TRADE_SERVICE, 15, "Installment count cannot be empty"),
    DUPLICATE_APPLICATION_NUMBER(TradeLoanErrorCategory.TRADE_SERVICE, 16, "Application number {0} already exists"),
    STALE_APPLICATION_NUMBER_ALLOCATION(
            TradeLoanErrorCategory.TRADE_SERVICE,
            17,
            "Application number {0} was allocated but its facility is in a terminal/cancelled state "
                    + "(no active loan file). Trigger compensation or cleanup for that facility before "
                    + "retrying origination."),
    DISBURSE_DESTINATION_DEPOSIT_IS_CLOSED(
            TradeLoanErrorCategory.TRADE_SERVICE, 18, "Disburse destination deposit is closed. deposit: {0}. "),
    INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE(
            TradeLoanErrorCategory.TRADE_SERVICE, 19, "Economic sector {0} is not allowed for loan type {1}"),
    APPLICATION_NUMBER_MISMATCH(
            TradeLoanErrorCategory.TRADE_SERVICE,
            20,
            "Application number mismatch. Command provided: {0}, Generated: {1}"),
    APPLICATION_NUMBER_CREATION_FAILED(
            TradeLoanErrorCategory.TRADE_SERVICE, 21, "Failed to create application number: {0}"),
    ECONOMIC_SECTOR_IS_PARENT(
            TradeLoanErrorCategory.TRADE_SERVICE,
            22,
            "The selected economic sector {0} is a parent sector. Please select a child sector."),
    INVALID_ACCOUNT_NUMBER(TradeLoanErrorCategory.TRADE_SERVICE, 23, "Invalid account number: {0}"),
    PRODUCT_NOT_SERVED_BY_THIS_USE_CASE(
            TradeLoanErrorCategory.TRADE_SERVICE,
            24,
            "Product {0} is not originated through this endpoint, which serves products whose schedule source "
                    + "is {1}. Submit it to the endpoint matching the product."),
    APPLICANT_BLACKLISTED(
            TradeLoanErrorCategory.TRADE_SERVICE,
            29,
            "Party with national code {0} ({1}) is blacklisted and cannot participate in facility origination"),
    APPLICANT_INCAPABLE(
            TradeLoanErrorCategory.TRADE_SERVICE,
            30,
            "Party with national code {0} ({1}) is legally incapable and cannot participate in facility origination"),
    APPLICANT_GRAYLISTED(
            TradeLoanErrorCategory.TRADE_SERVICE,
            31,
            "Party with national code {0} ({1}) is graylisted and cannot participate in facility origination"),
    APPLICANT_INACTIVE(
            TradeLoanErrorCategory.TRADE_SERVICE,
            32,
            "Party with national code {0} ({1}) is not an active customer and cannot participate in facility origination");

    private final ErrorCategory category;
    private final int sequence;
    private final String defaultMessageFormat;

    OriginateLoanFacilityErrorCodes(ErrorCategory category, int sequence, String defaultMessageFormat) {
        this.category = category;
        this.sequence = sequence;
        this.defaultMessageFormat = defaultMessageFormat;
        category.validate();
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

    @Override
    public int sequence() {
        return sequence;
    }

    @Override
    @NonNull
    public String getDefaultMessageFormat() {
        return defaultMessageFormat;
    }
}
