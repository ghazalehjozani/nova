package ir.dotin.loan.trade.core.application.service.establishloanarrangement.i18n;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

public enum LoanArrangementErrorCodes implements LocalizedMessage<LoanArrangementErrorCodes> {
    DUPLICATE_CODE("Loan arrangement with code {0} already exists"),
    MAPPING_ERROR("Failed to map command to domain model: {0}");

    private final String messageFormat;

    LoanArrangementErrorCodes(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    @Override
    public @NonNull String getDefaultMessageFormat() {
        return messageFormat;
    }
}
