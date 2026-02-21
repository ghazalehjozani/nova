package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class ReverseTransactionRequest extends FcbKafkaBaseRequest {

    private final String transactionNumber;

    public ReverseTransactionRequest(String transactionNumber) {
        super("cancel-transfer-money-loan");
        this.transactionNumber = transactionNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }
}
