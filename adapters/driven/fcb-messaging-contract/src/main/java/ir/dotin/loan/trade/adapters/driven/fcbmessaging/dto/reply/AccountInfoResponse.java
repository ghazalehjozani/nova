package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class AccountInfoResponse extends FcbBaseResponse {

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
