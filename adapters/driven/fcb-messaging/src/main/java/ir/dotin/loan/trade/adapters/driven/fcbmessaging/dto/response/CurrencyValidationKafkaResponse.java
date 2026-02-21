package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

import org.jspecify.annotations.Nullable;

public final class CurrencyValidationKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable Boolean allowed;
    private @Nullable String message;

    public @Nullable Boolean getAllowed() {
        return allowed;
    }

    public void setAllowed(@Nullable Boolean allowed) {
        this.allowed = allowed;
    }

    public @Nullable String getMessage() {
        return message;
    }

    public void setMessage(@Nullable String message) {
        this.message = message;
    }
}
