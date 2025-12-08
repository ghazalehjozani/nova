package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum FcbErrorCode {
    CMPLEXPEND_021(FcbBusinessLocalizedMessageCodes.UNKNOWN_ERROR),
    CMPLEXPEND_113(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER, List.of("depositNumber")),
    CMPLEXPEND_114(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER, List.of("depositNumber")),
    CMPLEXPEND_744(FcbBusinessLocalizedMessageCodes.INVALID_SUB_SOURCE_CODE, List.of("resourceCode")),
    CMPLEXPEND_729(
            FcbBusinessLocalizedMessageCodes.UNSUPPORTED_ECONOMICAL_SECTOR_FOR_LOAN_TYPE,
            List.of("loanTypeCode", "economicSectorCode")),
    CMPLEXPEND_758(FcbBusinessLocalizedMessageCodes.INVALID_CUSTOMER_NUMBER, List.of("customerNumber")),
    CMPLEXPEND_743(FcbBusinessLocalizedMessageCodes.INVALID_REQUEST_REASON, List.of("reasonTypeCode")),
    CMPLEXPEND_754(FcbBusinessLocalizedMessageCodes.INVALID_SERVICE_INPUT_FOR_PARAMETER_SERVICE_OPEN_ACCOUNT),
    CMPLEXPEND_753(FcbBusinessLocalizedMessageCodes.INVALID_ROLLBACK_ID, List.of("rollBackId")),
    CMPLEXPEND_755(FcbBusinessLocalizedMessageCodes.ROLLBACK_ID_AND_ACCOUNT_NUMBER_BOTH_PRESENT),
    CMPLEXPEND_757(FcbBusinessLocalizedMessageCodes.INVALID_ACCOUNT_NUMBER, List.of("accountNumber")),
    CMPLEXPEND_760(FcbBusinessLocalizedMessageCodes.INVALID_FILE_NUMBER, List.of("fileNumber")),
    CMPLEXPEND_763(FcbBusinessLocalizedMessageCodes.INVALID_SERVICE_INPUT_FOR_UN_RESERVE),
    LO_772(FcbBusinessLocalizedMessageCodes.INVALID_ECONOMICAL_SECTOR_CODE, List.of("economicSectorCode")),
    DE_35(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER, List.of("depositNumber")),
    DE_06(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER, List.of("depositNumber")),
    DE_10(FcbBusinessLocalizedMessageCodes.UNSUPPORTED_CURRENCY_FOR_DEPOSIT, List.of("depositNumber", "currencyCode"));

    private final FcbBusinessLocalizedMessageCodes messageCode;
    private final List<String> contextKeys;

    FcbErrorCode(FcbBusinessLocalizedMessageCodes messageCode) {
        this.messageCode = messageCode;
        this.contextKeys = Collections.emptyList();
    }

    FcbErrorCode(FcbBusinessLocalizedMessageCodes messageCode, List<String> contextKeys) {
        this.messageCode = messageCode;
        this.contextKeys = contextKeys != null ? contextKeys : Collections.emptyList();
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
