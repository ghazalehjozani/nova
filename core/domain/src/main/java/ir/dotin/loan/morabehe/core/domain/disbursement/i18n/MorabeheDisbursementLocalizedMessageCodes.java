package ir.dotin.loan.morabehe.core.domain.disbursement.i18n;

import ir.dotin.platform.domain.common.i18n.LocalizedMessage;
import ir.dotin.loan.baseloan.core.domain.shared.BaseLoanValidationConstants;

public enum MorabeheDisbursementLocalizedMessageCodes
        implements LocalizedMessage<MorabeheDisbursementLocalizedMessageCodes> {
    INVALID_STATE_FOR_COMPLETION(
            "status.invalid_state_completion", "Cannot complete Morabehe disbursement (ID: {0}) in state: {1}"),
    INVALID_STATE_FOR_PENDING(
            "status.invalid_state_pending", "Cannot mark pending Morabehe disbursement (ID: {0}) in state: {1}"),
    INVALID_STATE_FOR_FAILURE(
            "status.invalid_state_failure", "Cannot mark failed Morabehe disbursement (ID: {0}) in state: {1}"),
    TRANSACTION_NUMBERS_NULL_ON_COMPLETION(
            "completion.transaction_numbers_null",
            "Transaction Numbers cannot be null or empty for completed Morabehe disbursement (ID: {0})."),
    FAILURE_REASON_NULL(
            "failure.reason_null", "Failure reason cannot be null for failed Morabehe disbursement (ID: {0})."),
    FIELD_REQUIRED("validation.fieldRequired", "Field ''{0}'' is required for disbursement."),
    COLLECTION_NULL_ELEMENTS("validation.collection_empty", "Collection ''{0}'' cannot contain null elements.");

    private final String constructedKey;
    private final String messageFormat;

    MorabeheDisbursementLocalizedMessageCodes(String key, String defaultMessageFormat) {
        this.constructedKey = BaseLoanValidationConstants.DISBURSEMENT_KEY_PREFIX + key;
        this.messageFormat = defaultMessageFormat;
    }

    @Override
    public String getKey() {
        return constructedKey;
    }

    @Override
    public String getDefaultMessageFormat() {
        return messageFormat;
    }
}
