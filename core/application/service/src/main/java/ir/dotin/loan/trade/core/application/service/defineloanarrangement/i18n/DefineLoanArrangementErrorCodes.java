package ir.dotin.loan.trade.core.application.service.defineloanarrangement.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum DefineLoanArrangementErrorCodes implements LocalizedMessage<DefineLoanArrangementErrorCodes> {
    DUPLICATE_CODE("Loan arrangement with code {0} already exists"),
    MAPPING_ERROR("Failed to map command to domain model: {0}");

    private final String defaultMessageFormat;
}
