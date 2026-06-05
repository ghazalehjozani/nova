package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class DepositClosedResponse extends FcbBaseResponse {

    private @Nullable Boolean closed;
    private @Nullable String currencyTypeCode;

    public @Nullable Boolean getClosed() {
        return closed;
    }

    public void setClosed(@Nullable Boolean closed) {
        this.closed = closed;
    }

    public @Nullable String getCurrencyTypeCode() {
        return currencyTypeCode;
    }

    public void setCurrencyTypeCode(@Nullable String currencyTypeCode) {
        this.currencyTypeCode = currencyTypeCode;
    }
}
