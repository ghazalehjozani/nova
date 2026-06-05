package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class CollateralSerialsResponse extends FcbBaseResponse {

    private @Nullable List<String> serials;

    public @Nullable List<String> getSerials() {
        return serials;
    }

    public void setSerials(@Nullable List<String> serials) {
        this.serials = serials;
    }
}
