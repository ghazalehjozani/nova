package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

import org.jspecify.annotations.Nullable;

public final class DeleteAccountRequest extends FcbKafkaBaseRequest {

    private final @Nullable String accountNumber;
    private final String transactionId;
    private final @Nullable String rollBackId;

    public DeleteAccountRequest(@Nullable String accountNumber, String transactionId, @Nullable String rollBackId) {
        super("nova-delete-account");
        this.accountNumber = accountNumber;
        this.transactionId = transactionId;
        this.rollBackId = rollBackId;
    }

    public @Nullable String getAccountNumber() {
        return accountNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public @Nullable String getRollBackId() {
        return rollBackId;
    }
}
