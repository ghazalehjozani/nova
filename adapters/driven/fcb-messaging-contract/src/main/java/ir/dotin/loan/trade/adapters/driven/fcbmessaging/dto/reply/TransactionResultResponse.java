package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class TransactionResultResponse extends FcbBaseResponse {

    private @Nullable String transactionCode;

    public @Nullable String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(@Nullable String transactionCode) {
        this.transactionCode = transactionCode;
    }
}
