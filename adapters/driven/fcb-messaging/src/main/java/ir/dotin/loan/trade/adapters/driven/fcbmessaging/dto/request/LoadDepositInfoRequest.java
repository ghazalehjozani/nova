package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class LoadDepositInfoRequest extends FcbKafkaBaseRequest {

    private final String depositNumber;

    public LoadDepositInfoRequest(String depositNumber) {
        super("load-deposit-by-number");
        this.depositNumber = depositNumber;
    }

    public String getDepositNumber() {
        return depositNumber;
    }
}
