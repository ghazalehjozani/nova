package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class LoadCollateralRequest extends FcbKafkaBaseRequest {

    private final String assuranceSerial;
    private final String uniqueTrackingCode;

    public LoadCollateralRequest(String assuranceSerial, String uniqueTrackingCode) {
        super("load-assurance-service");
        this.assuranceSerial = assuranceSerial;
        this.uniqueTrackingCode = uniqueTrackingCode;
    }

    public String getAssuranceSerial() {
        return assuranceSerial;
    }

    public String getUniqueTrackingCode() {
        return uniqueTrackingCode;
    }
}
