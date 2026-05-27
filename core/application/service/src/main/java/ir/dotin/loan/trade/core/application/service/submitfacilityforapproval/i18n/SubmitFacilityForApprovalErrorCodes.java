package ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.i18n;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.pangaea.commons.core.error.ErrorCategory;
import ir.dotin.platform.pangaea.commons.core.error.ProductErrorCode;
import ir.dotin.loan.trade.core.domain.shared.error.TradeLoanErrorCategory;

/*
 * TRADE_SERVICE (54) sub-range allocation:
 *   Originate (OriginateLoanFacilityErrorCodes): 001–025
 *   Submit (this file):                          026–028
 *   Reserved:                                    029–999
 *
 * numericCode = 54*1000 + sequence.
 */
public enum SubmitFacilityForApprovalErrorCodes implements ProductErrorCode<SubmitFacilityForApprovalErrorCodes> {
    FACILITY_NOT_FOUND(TradeLoanErrorCategory.TRADE_SERVICE, 26, "Facility with ID {0} not found"),
    FACILITY_ALREADY_SUBMITTED(TradeLoanErrorCategory.TRADE_SERVICE, 27, "Facility {0} is already submitted"),
    INVALID_STATE(TradeLoanErrorCategory.TRADE_SERVICE, 28, "Facility {0} is in invalid state for submission");

    private final ErrorCategory category;
    private final int sequence;
    private final String defaultMessageFormat;

    SubmitFacilityForApprovalErrorCodes(ErrorCategory category, int sequence, String defaultMessageFormat) {
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
