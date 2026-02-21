package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import java.util.List;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

import org.jspecify.annotations.Nullable;

public final class CollateralSerialsKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable List<String> serials;

    public @Nullable List<String> getSerials() {
        return serials;
    }

    public void setSerials(@Nullable List<String> serials) {
        this.serials = serials;
    }
}
