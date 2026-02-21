package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class LoadResourceRequest extends FcbKafkaBaseRequest {

    private final String resourceCode;

    public LoadResourceRequest(String resourceCode) {
        super("load-resource-by-code");
        this.resourceCode = resourceCode;
    }

    public String getResourceCode() {
        return resourceCode;
    }
}
