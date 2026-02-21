package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.util.List;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class FindRelatedCustomersRequest extends FcbKafkaBaseRequest {

    private final List<String> customerNumbers;

    public FindRelatedCustomersRequest(List<String> customerNumbers) {
        super("find-related-customers");
        this.customerNumbers = customerNumbers;
    }

    public List<String> getCustomerNumbers() {
        return customerNumbers;
    }
}
