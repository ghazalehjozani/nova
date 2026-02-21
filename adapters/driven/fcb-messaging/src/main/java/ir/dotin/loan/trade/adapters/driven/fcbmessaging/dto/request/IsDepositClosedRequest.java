package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class IsDepositClosedRequest extends FcbKafkaBaseRequest {

    private final String depositNumber;
    private final String currencySwiftCode;

    public IsDepositClosedRequest(String depositNumber, String currencySwiftCode) {
        super("is-deposit-closed");
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
