package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class ResourceResponse extends FcbBaseResponse {

    private @Nullable String code;

    public @Nullable String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }
}
