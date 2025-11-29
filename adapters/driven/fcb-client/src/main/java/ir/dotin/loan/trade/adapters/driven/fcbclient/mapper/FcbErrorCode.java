package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Getter
public enum FcbErrorCode {
    CMPLEXPEND_021(FcbBusinessLocalizedMessageCodes.UNKNOWN_ERROR),
    CMPLEXPEND_113(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER, Set.of("depositNumber")),
    CMPLEXPEND_114(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER, Set.of("depositNumber")),
    CMPLEXPEND_744(FcbBusinessLocalizedMessageCodes.INVALID_SUB_SOURCE_CODE, Set.of("resourceCode")),
    CMPLEXPEND_729(FcbBusinessLocalizedMessageCodes.UNSUPPORTED_ECONOMICAL_SECTOR_FOR_LOAN_TYPE, Set.of("loanTypeCode", "economicSectorCode")),
    CMPLEXPEND_758(FcbBusinessLocalizedMessageCodes.INVALID_CUSTOMER_NUMBER, Set.of("customerNumber")),
    CMPLEXPEND_743(FcbBusinessLocalizedMessageCodes.INVALID_REQUEST_REASON, Set.of("reasonTypeCode")),
    CMPLEXPEND_754(FcbBusinessLocalizedMessageCodes.INVALID_SERVICE_INPUT_FOR_PARAMETER_SERVICE_OPEN_ACCOUNT),
    CMPLEXPEND_753(FcbBusinessLocalizedMessageCodes.INVALID_ROLLBACK_ID, Set.of("rollBackId")),
    CMPLEXPEND_755(FcbBusinessLocalizedMessageCodes.ROLLBACK_ID_AND_ACCOUNT_NUMBER_BOTH_PRESENT),
    CMPLEXPEND_760(FcbBusinessLocalizedMessageCodes.INVALID_ACCOUNT_NUMBER, Set.of("accountNumber")),
    LO_772(FcbBusinessLocalizedMessageCodes.INVALID_ECONOMICAL_SECTOR_CODE, Set.of("economicSectorCode")),
    DE_35(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER, Set.of("depositNumber")),
    DE_06(FcbBusinessLocalizedMessageCodes.INVALID_DEPOSIT_NUMBER, Set.of("depositNumber")),
    DE_10(FcbBusinessLocalizedMessageCodes.UNSUPPORTED_CURRENCY_FOR_DEPOSIT, Set.of("depositNumber", "currencyCode"));

    private final FcbBusinessLocalizedMessageCodes messageCode;
    private final Set<String> contextKeys;

    FcbErrorCode(FcbBusinessLocalizedMessageCodes messageCode) {
        this.messageCode = messageCode;
        this.contextKeys = Collections.emptySet();
    }

    FcbErrorCode(FcbBusinessLocalizedMessageCodes messageCode, Set<String> contextKeys) {
        this.messageCode = messageCode;
        this.contextKeys = contextKeys != null ? contextKeys : Collections.emptySet();
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
