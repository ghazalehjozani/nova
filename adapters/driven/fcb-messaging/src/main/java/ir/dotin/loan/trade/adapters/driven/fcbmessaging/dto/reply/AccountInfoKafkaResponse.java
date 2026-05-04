package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

public final class AccountInfoKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String accountNumber;
    private @Nullable String id;

    public @Nullable String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(@Nullable String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public @Nullable String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }
}
