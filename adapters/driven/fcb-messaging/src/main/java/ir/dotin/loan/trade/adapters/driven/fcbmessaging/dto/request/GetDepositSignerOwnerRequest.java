package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class GetDepositSignerOwnerRequest extends FcbKafkaBaseRequest {

    private final String depositNumber;

    public GetDepositSignerOwnerRequest(String depositNumber) {
        super("get-all-deposit-signer-owner-customer");
        this.depositNumber = depositNumber;
    }

    public String getDepositNumber() {
        return depositNumber;
    }
}
