package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class BatchCloseAccountResponse extends FcbBaseResponse {

    private @Nullable List<Item> results;
    private int requested;
    private int succeeded;

    public @Nullable List<Item> getResults() {
        return results;
    }

    public void setResults(@Nullable List<Item> results) {
        this.results = results;
    }

    public int getRequested() {
        return requested;
    }

    public void setRequested(int requested) {
        this.requested = requested;
    }

    public int getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(int succeeded) {
        this.succeeded = succeeded;
    }

    public static final class Item {

        private @Nullable String transactionId;
        private @Nullable String accountNumber;

        public @Nullable String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(@Nullable String transactionId) {
            this.transactionId = transactionId;
        }

        public @Nullable String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(@Nullable String accountNumber) {
            this.accountNumber = accountNumber;
        }
    }
}
