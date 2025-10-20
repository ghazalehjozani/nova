package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.ValueObject;
import ir.dotin.loan.baseloan.core.domain.shared.i18n.ValidationLocalizedMessageCodes;

import static java.util.Objects.requireNonNull;

public record DepositClosedStatus(boolean isClosed, @NonNull String currencySwiftCode)
        implements ValueObject<DepositClosedStatus> {

    public DepositClosedStatus {
        requireNonNull(currencySwiftCode, "Currency swift code cannot be null in constructor");
    }

    public static Result<DepositClosedStatus> of(boolean isClosed, @NonNull String rawCurrencySwiftCode) {
        Notification preliminaryNotification = Notification.create();

        if (rawCurrencySwiftCode.isBlank()) {
            preliminaryNotification.addError(
                    ValidationLocalizedMessageCodes.VALIDATION_MESSAGE_EMPTY,
                    "Currency swift code cannot be null or blank");
        }

        if (preliminaryNotification.hasErrors()) {
            return Result.failure(preliminaryNotification);
        }

        DepositClosedStatus status = new DepositClosedStatus(isClosed, rawCurrencySwiftCode);

        Notification internalValidation = status.validate();
        if (internalValidation.hasErrors()) {
            return Result.failure(internalValidation);
        }

        return Result.success(status);
    }
}
