package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class LoadReasonTypeForCreateRequest extends FcbKafkaBaseRequest {

    private final String reasonTypeCode;

    public LoadReasonTypeForCreateRequest(String reasonTypeCode) {
        super("load-reason-type-for-create");
        this.reasonTypeCode = reasonTypeCode;
    }

    public String getReasonTypeCode() {
        return reasonTypeCode;
    }
}
