package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import java.util.Objects;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.loan.trade.adapters.driven.fcbclient.context.FcbContext;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FcbErrorCodeMapper {

    public Notification mapRsCodeToNotification(FcbBaseResponse response, FcbContext context) {
        Objects.requireNonNull(response, "FcbBaseResponse cannot be null");

        String rsCode = response.getRsCode();

        if (Objects.isNull(rsCode) || rsCode.trim().isEmpty()) {
            return Notification.ofError(FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, context.toObjectArray());
        }

        FcbBusinessLocalizedMessageCodes errorCode = FcbErrorCode.fromCode(rsCode)
                .map(FcbErrorCode::getMessageCode)
                .orElse(FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR);

        String errorMessage = response.getErrorMessage();
        Object[] contextArgs = context.hasContent() ? context.toObjectArray() : new Object[] {errorMessage};

        return Notification.ofError(errorCode, contextArgs);
    }

}
