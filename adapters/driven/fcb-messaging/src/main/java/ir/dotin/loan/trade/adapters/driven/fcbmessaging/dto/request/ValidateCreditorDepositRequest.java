package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.math.BigDecimal;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class ValidateCreditorDepositRequest extends FcbKafkaBaseRequest {

    private final String depositNumber;
    private final String currencySwiftCode;
    private final BigDecimal amount;

    public ValidateCreditorDepositRequest(String depositNumber, String currencySwiftCode, BigDecimal amount) {
        super("validate-creditor-deposit");
        this.depositNumber = depositNumber;
        this.currencySwiftCode = currencySwiftCode;
        this.amount = amount;
    }

    public String getDepositNumber() {
        return depositNumber;
    }

    public String getCurrencySwiftCode() {
        return currencySwiftCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
