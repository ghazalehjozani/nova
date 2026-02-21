package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class FetchSanctionDetailsRequest extends FcbKafkaBaseRequest {

    private final String sanctionSerial;

    public FetchSanctionDetailsRequest(String sanctionSerial) {
        super("fetch-sanction-details");
        this.sanctionSerial = sanctionSerial;
    }

    public String getSanctionSerial() {
        return sanctionSerial;
    }
}
