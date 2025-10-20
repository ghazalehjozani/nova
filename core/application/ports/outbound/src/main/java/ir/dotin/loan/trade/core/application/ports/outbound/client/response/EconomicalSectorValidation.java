package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.ValueObject;
import ir.dotin.loan.baseloan.core.domain.shared.i18n.ValidationLocalizedMessageCodes;

public record EconomicalSectorValidation(boolean isValid, @Nullable String message)
        implements ValueObject<EconomicalSectorValidation> {

    public static Result<EconomicalSectorValidation> of(boolean isValid, @Nullable String rawMessage) {
        Notification preliminaryNotification = Notification.create();

        if (rawMessage != null && rawMessage.isBlank()) {
            preliminaryNotification.addError(ValidationLocalizedMessageCodes.VALIDATION_MESSAGE_EMPTY);
        }

        if (preliminaryNotification.hasErrors()) {
            return Result.failure(preliminaryNotification);
        }

        EconomicalSectorValidation validation = new EconomicalSectorValidation(isValid, rawMessage);

        Notification internalValidation = validation.validate();
        if (internalValidation.hasErrors()) {
            return Result.failure(internalValidation);
        }

        return Result.success(validation);
    }

    public static Result<EconomicalSectorValidation> valid(@NonNull String message) {
        return of(true, message);
    }

    public static Result<EconomicalSectorValidation> invalid(@NonNull String message) {
        return of(false, message);
    }

    public static Result<EconomicalSectorValidation> valid() {
        return of(true, "Validation successful");
    }

    public static Result<EconomicalSectorValidation> invalid() {
        return of(false, "Validation failed");
    }
}
