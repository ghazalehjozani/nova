package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class ValidateDebtorDepositRequest extends FcbKafkaBaseRequest {

    private final String depositNumber;
    private final String currencySwiftCode;

    public ValidateDebtorDepositRequest(String depositNumber, String currencySwiftCode) {
        super("validate-debtor-deposit");
        this.depositNumber = depositNumber;
        this.currencySwiftCode = currencySwiftCode;
    }

    public String getDepositNumber() {
        return depositNumber;
    }

    public String getCurrencySwiftCode() {
        return currencySwiftCode;
    }
}
