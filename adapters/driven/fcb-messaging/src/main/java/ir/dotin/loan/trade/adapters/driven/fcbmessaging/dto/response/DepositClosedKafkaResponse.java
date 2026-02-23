package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

public final class DepositClosedKafkaResponse extends FcbKafkaBaseResponse {

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
