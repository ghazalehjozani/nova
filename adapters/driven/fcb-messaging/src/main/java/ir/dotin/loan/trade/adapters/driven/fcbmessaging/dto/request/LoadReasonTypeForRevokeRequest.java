package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class LoadReasonTypeForRevokeRequest extends FcbKafkaBaseRequest {

    private final String reasonTypeCode;

    public LoadReasonTypeForRevokeRequest(String reasonTypeCode) {
        super("load-reason-type-for-revoke");
        this.reasonTypeCode = reasonTypeCode;
    }

    public String getReasonTypeCode() {
        return reasonTypeCode;
    }
}
