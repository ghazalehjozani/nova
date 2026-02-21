package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class LoadEconomicSectorRequest extends FcbKafkaBaseRequest {

    private final String economicalSectionCode;

    public LoadEconomicSectorRequest(String economicalSectionCode) {
        super("load-economicalSection-by-code");
        this.economicalSectionCode = economicalSectionCode;
    }

    public String getEconomicalSectionCode() {
        return economicalSectionCode;
    }
}
