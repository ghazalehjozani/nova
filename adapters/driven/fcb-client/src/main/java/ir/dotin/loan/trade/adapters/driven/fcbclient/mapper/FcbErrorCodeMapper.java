package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import java.util.*;

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

        FcbErrorCode fcbErrorCode = FcbErrorCode.fromCode(rsCode).orElse(null);

        FcbBusinessLocalizedMessageCodes messageCode = fcbErrorCode != null
                ? fcbErrorCode.getMessageCode()
                : FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR;

        String errorMessage = response.getErrorMessage();
        Object[] contextArgs = context.hasContent() ? context.toObjectArray() : new Object[] {errorMessage};

        Object[] mappedArgs =
                fcbErrorCode != null ? extractContextArgsByKeys(context, fcbErrorCode.getContextKeys()) : contextArgs;

        return Notification.ofError(messageCode, mappedArgs);
    }

    private Object[] extractContextArgsByKeys(FcbContext context, List<String> contextKeys) {
        if (contextKeys == null || contextKeys.isEmpty()) {
            return context.toObjectArray();
        }

        Map<String, Object> additionalContext = context.getAdditionalContext();
        if (additionalContext == null) {
            return context.toObjectArray();
        }

        List<Object> validValues = new ArrayList<>();
        for (String key : contextKeys) {
            Object value = additionalContext.get(key);
            if (value != null) {
                validValues.add(value);
            }
        }

        return validValues.isEmpty() ? context.toObjectArray() : validValues.toArray();
    }
}
