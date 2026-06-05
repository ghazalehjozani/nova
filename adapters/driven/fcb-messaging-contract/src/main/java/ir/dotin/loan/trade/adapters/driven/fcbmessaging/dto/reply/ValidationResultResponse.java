package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class ValidationResultResponse extends FcbBaseResponse {

    private @Nullable Boolean valid;
    private @Nullable String message;

    public @Nullable Boolean getValid() {
        return valid;
    }

    public void setValid(@Nullable Boolean valid) {
        this.valid = valid;
    }

    public @Nullable String getMessage() {
        return message;
    }

    public void setMessage(@Nullable String message) {
        this.message = message;
    }
}
