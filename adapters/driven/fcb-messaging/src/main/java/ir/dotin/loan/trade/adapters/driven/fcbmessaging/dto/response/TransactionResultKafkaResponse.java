package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

import org.jspecify.annotations.Nullable;

public final class TransactionResultKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String transactionCode;

    public @Nullable String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(@Nullable String transactionCode) {
        this.transactionCode = transactionCode;
    }
}
