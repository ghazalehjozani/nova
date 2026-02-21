package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.util.List;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class HasDepositAllowedCurrenciesRequest extends FcbKafkaBaseRequest {

    private final String depositNumber;
    private final List<String> currencies;

    public HasDepositAllowedCurrenciesRequest(String depositNumber, List<String> currencies) {
        super("has-deposit-allowed-currencies");
        this.depositNumber = depositNumber;
        this.currencies = currencies;
    }

    public String getDepositNumber() {
        return depositNumber;
    }

    public List<String> getCurrencies() {
        return currencies;
    }
}
