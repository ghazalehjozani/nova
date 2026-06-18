package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class EvaluateFormulaViaFcbResponse extends FcbBaseResponse {

    private @Nullable String result;

    public @Nullable String getResult() {
        return result;
    }

    public void setResult(@Nullable String result) {
        this.result = result;
    }
}
