package ir.dotin.loan.trade.core.application.service.shared.i18n;

import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SharedErrorCodes implements LocalizedMessage<SharedErrorCodes> {
    POST_TITLE_CREATION_FAILED("Failed to create post title: {0}");

    private final String defaultMessageFormat;
}
