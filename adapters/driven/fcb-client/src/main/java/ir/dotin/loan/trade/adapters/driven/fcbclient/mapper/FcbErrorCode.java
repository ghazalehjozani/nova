package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import java.util.Optional;

import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum FcbErrorCode {
    CMPLEXPEND_021(FcbBusinessLocalizedMessageCodes.UNKNOWN_ERROR), // TODO not Business like LO_98 in fcb
    CMPLEXPEND_113(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER),
    CMPLEXPEND_114(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER),
    CMPLEXPEND_744(FcbBusinessLocalizedMessageCodes.INVALID_SUB_SOURCE_CODE),
    CMPLEXPEND_729(FcbBusinessLocalizedMessageCodes.UNSUPPORTED_ECONOMICAL_SECTOR_FOR_LOAN_TYPE),
    CMPLEXPEND_758(FcbBusinessLocalizedMessageCodes.INVALID_CUSTOMER_NUMBER),
    CMPLEXPEND_743(FcbBusinessLocalizedMessageCodes.INVALID_REQUEST_REASON),
    LO_772(FcbBusinessLocalizedMessageCodes.INVALID_ECONOMICAL_SECTOR_CODE),
    DE_35(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER),
    DE_06(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER),
    DE_10(FcbBusinessLocalizedMessageCodes.UNSUPPORTED_CURRENCY_FOR_DEPOSIT);

    private final FcbBusinessLocalizedMessageCodes messageCode;

    FcbErrorCode(FcbBusinessLocalizedMessageCodes messageCode) {
        this.messageCode = messageCode;
    }

    public String getCode() {
        return name();
    }

    public static Optional<FcbErrorCode> fromCode(String code) {
        try {
            return Optional.of(valueOf(code));
        } catch (IllegalArgumentException e) {
            log.debug("Unknown FCB error code: {}", code);
            return Optional.empty();
        }
    }
}
