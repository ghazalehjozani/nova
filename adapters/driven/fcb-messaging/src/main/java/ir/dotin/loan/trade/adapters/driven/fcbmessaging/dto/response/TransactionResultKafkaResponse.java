package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

public final class TransactionResultKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String transactionCode;

    public @Nullable String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(@Nullable String transactionCode) {
        this.transactionCode = transactionCode;
    }
}
