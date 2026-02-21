package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class ValidateAccountNumberRequest extends FcbKafkaBaseRequest {

    private final String accountNumber;

    public ValidateAccountNumberRequest(String accountNumber) {
        super("load-account-by-account-number-service");
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
