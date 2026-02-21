package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class LoadCustomerBirthInfoRequest extends FcbKafkaBaseRequest {

    private final String customerNumber;

    public LoadCustomerBirthInfoRequest(String customerNumber) {
        super("load-customer-birth-info");
        this.customerNumber = customerNumber;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }
}
