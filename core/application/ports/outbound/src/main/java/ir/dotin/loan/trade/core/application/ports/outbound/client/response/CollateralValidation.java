package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.ValueObject;
import ir.dotin.loan.baseloan.core.domain.shared.i18n.ValidationLocalizedMessageCodes;

public record CollateralValidation(boolean isValid, @Nullable String message)
        implements ValueObject<CollateralValidation> {

    public static Result<CollateralValidation> of(boolean isValid, @Nullable String rawMessage) {
        Notification preliminaryNotification = Notification.create();

        // Message can be null, but if provided should not be blank
        if (rawMessage != null && rawMessage.isBlank()) {
            preliminaryNotification.addError(ValidationLocalizedMessageCodes.VALIDATION_MESSAGE_EMPTY);
        }

        if (preliminaryNotification.hasErrors()) {
            return Result.failure(preliminaryNotification);
        }

        CollateralValidation validation = new CollateralValidation(isValid, rawMessage);

        Notification internalValidation = validation.validate();
        if (internalValidation.hasErrors()) {
            return Result.failure(internalValidation);
        }

        return Result.success(validation);
    }

    public static Result<CollateralValidation> valid(@NonNull String message) {
        return of(true, message);
    }

    public static Result<CollateralValidation> invalid(@NonNull String message) {
        return of(false, message);
    }

    public static Result<CollateralValidation> valid() {
        return of(true, "Assurance validation successful");
    }

    public static Result<CollateralValidation> invalid() {
        return of(false, "Assurance validation failed");
    }
}
