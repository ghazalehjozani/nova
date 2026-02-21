package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

import org.jspecify.annotations.Nullable;

public final class ValidationResultKafkaResponse extends FcbKafkaBaseResponse {

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
