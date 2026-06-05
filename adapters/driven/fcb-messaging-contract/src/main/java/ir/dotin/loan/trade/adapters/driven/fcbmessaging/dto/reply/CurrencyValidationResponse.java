package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class CurrencyValidationResponse extends FcbBaseResponse {

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
