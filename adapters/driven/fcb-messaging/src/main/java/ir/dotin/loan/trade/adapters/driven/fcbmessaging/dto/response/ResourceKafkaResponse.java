package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

public final class ResourceKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String code;

    public @Nullable String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }
}
